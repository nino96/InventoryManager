package com.example.android.inventorymanager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.support.design.widget.TextInputEditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.inventorymanager.Models.SchemaEntry;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity implements View.OnClickListener{

    private LinkedHashMap<TextInputEditText,String> fields;
    private LinkedHashMap<String,Object> itemList;

    private String mBusinessName;

    private int price;
    private int quantity;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSchemaReference;
    private DatabaseReference mTransactionReference;
    private DatabaseReference mItemsReference;

    private LinearLayout mAddItemLayout;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mFirebaseDatabase = Utils.getDatabase();

        fields = new LinkedHashMap<>();
        itemList = new LinkedHashMap<>();

        mAddItemLayout = (LinearLayout) findViewById(R.id.add_item_form);
        mSubmitButton = (Button) findViewById(R.id.bt_add_item);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mBusinessName = prefs.getString("businessName",null);

        mSchemaReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("schema");
        mTransactionReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("transactions").child("outflow");
        mItemsReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("items");

        createAddItemFormLayout(mAddItemLayout);


    }

    private void createAddItemFormLayout(LinearLayout mAddItemLayout){
        TextInputLayout textInputLayout;
        TextInputEditText et;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //no need to sync schema since it is static once business created, for now
        for(SchemaEntry entry:Utils.schemaEntryList){

            et = new TextInputEditText(this);
            et.setLayoutParams(layoutParams);
            et.setHint(entry.fieldName);

            Log.v("Add Item",entry.fieldName+" "+entry.fieldType);


            if(entry.fieldType.equals("String")){
                et.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            else if(entry.fieldType.equals("Integer")){
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            //after setting input type add textwatcher which depends on input type
            et.addTextChangedListener(new MyTextWatcher(et));
            //add to field list, for validation calls on all on pressing submit button

            /*if(entry.required.equals("true"))
                fields.put(et,true);
            else
                fields.put(et,false);*/

            fields.put(et,entry.fieldName);

            textInputLayout = new TextInputLayout(this);
            textInputLayout.setLayoutParams(layoutParams);
            textInputLayout.addView(et);

            mAddItemLayout.addView(textInputLayout);

        }

        Button addItemButton = new Button(this);
        addItemButton.setId(R.id.bt_add_item);
        addItemButton.setText("Add Item");
        addItemButton.setOnClickListener(this);
        mAddItemLayout.addView(addItemButton);
    }

    private boolean validateNumberField(TextInputEditText view){

        TextInputLayout parent = (TextInputLayout) view.getParent().getParent();
        int n;
        
        try {
            n = Integer.parseInt(view.getText().toString());
        }
        catch (NumberFormatException e){
            view.setError("Enter " + parent.getHint() + "(>0)");
            view.requestFocus();
            return false;
        }


        if (view.getText().toString().trim().isEmpty() || n<=0) {
            view.setError("Enter " + parent.getHint() + "(>0)");
            view.requestFocus();
            return false;
        }
        else {
            if(parent.getHint().equals(getString(R.string.field_price)))
                price = n;
            else if(parent.getHint().equals(getString(R.string.field_quantity)))
                quantity = n;

            //remove error from parent TextInputLayout
            parent.setErrorEnabled(false);
        }


        return true;
    }

    private boolean validateTextField(TextInputEditText view){

        TextInputLayout parent = (TextInputLayout) view.getParent().getParent();

        //if empty and the field is required by the schema
        if (view.getText().toString().trim().isEmpty()) {
            view.setError("Enter " + parent.getHint());
            view.requestFocus();
            return false;
        }
        else {
            //remove error from parent TextInputLayout
            parent.setErrorEnabled(false);
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        int flag = 1;
        String fieldName;
        String fieldValue;

        if(v.getId()==R.id.bt_add_item){
            for(Map.Entry<TextInputEditText,String> entry : fields.entrySet())
            {
                fieldName = entry.getValue();
                fieldValue = entry.getKey().getText().toString();

                if(entry.getKey().getInputType() == InputType.TYPE_CLASS_NUMBER)
                {
                    if(!validateNumberField(entry.getKey()))
                        flag = 0;
                    else
                        itemList.put(fieldName,Integer.parseInt(fieldValue));
                }
                else if(entry.getKey().getInputType() == InputType.TYPE_CLASS_TEXT)
                {
                    if(!validateTextField(entry.getKey()))
                        flag = 0;
                    else
                        itemList.put(fieldName,fieldValue);
                }
            }

            if(flag==1){
                mItemsReference.push().setValue(itemList);

                HashMap<String,Object> transaction = new LinkedHashMap<>();
                transaction.put("timestamp",ServerValue.TIMESTAMP);
                transaction.put("amount",price*quantity);
                mTransactionReference.push().setValue(transaction);

                Toast.makeText(getApplicationContext(),"Item Added",Toast.LENGTH_SHORT).show();
                //finish(); allow user to add another item
                for(Map.Entry<TextInputEditText,String> entry : fields.entrySet())
                {

                    entry.getKey().setText("");
                }

            }
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private TextInputEditText view;

        private MyTextWatcher(TextInputEditText view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch ((view.getInputType())) {
                case InputType.TYPE_CLASS_NUMBER:
                    validateNumberField(view);
                    break;

                case InputType.TYPE_CLASS_TEXT:
                    validateTextField(view);
                    break;
            }
        }
    }
}
