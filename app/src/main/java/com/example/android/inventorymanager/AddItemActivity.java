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

import com.example.android.inventorymanager.Models.InventoryListItem;
import com.example.android.inventorymanager.Models.SchemaEntry;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity implements View.OnClickListener{

    private LinkedHashMap<TextInputEditText,String> fields;
    private LinkedHashMap<String,Object> itemList;

    private String mBusinessName;




    //the compulsory fields of the item
    private int price;
    private int quantity;
    private String itemName;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSchemaReference;
    private DatabaseReference mTransactionReference;
    private DatabaseReference mItemsReference;
    private DatabaseReference mCountReference;

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


        //get schema list again
        Utils.getSchemaEntryList(mFirebaseDatabase.getReference().child("businesses"),mBusinessName);




        mSchemaReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("schema");
        mTransactionReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("transactions");
        mItemsReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName).child("items");
        mCountReference = mFirebaseDatabase.getReference("businesses").child(mBusinessName);
        mItemsReference.keepSynced(true);

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

            Log.v("Schema",entry.fieldName);
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
            Log.v("Add Item",n+"");
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

        if(Utils.isOnline()) {
            if (v.getId() == R.id.bt_add_item) {
                for (Map.Entry<TextInputEditText, String> entry : fields.entrySet()) {
                    fieldName = entry.getValue();
                    fieldValue = entry.getKey().getText().toString();

                    if (entry.getKey().getInputType() == InputType.TYPE_CLASS_NUMBER) {
                        if (!validateNumberField(entry.getKey()))
                            flag = 0;
                        else {

                            if (((TextInputLayout) entry.getKey().getParent().getParent()).getHint().equals(InventoryListItem.PRICE_FIELD)) {
                                price = Integer.parseInt(fieldValue);
                            } else if (((TextInputLayout) entry.getKey().getParent().getParent()).getHint().equals(InventoryListItem.QUANTITY_FIELD)) {
                                quantity = Integer.parseInt(fieldValue);
                            }
                            Log.v("AddDebug", fieldValue);
                            itemList.put(fieldName, Integer.parseInt(fieldValue));
                        }
                    } else if (entry.getKey().getInputType() == InputType.TYPE_CLASS_TEXT) {
                        if (!validateTextField(entry.getKey()))
                            flag = 0;
                        else {
                            TextInputLayout parent = (TextInputLayout) entry.getKey().getParent().getParent();
                            if (parent.getHint().equals(InventoryListItem.NAME_FIELD)) {
                                //don't put in the list if the field is name since name is being used as key now
                                itemName = fieldValue;
                            }
                            itemList.put(fieldName, fieldValue);

                        }
                    }
                }

                if (flag == 1) {




                    mItemsReference.child(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                                Toast.makeText(getApplicationContext(), "Product already exists, view inventory", Toast.LENGTH_SHORT).show();
                            else {
                                int count = 0;
                                for (Map.Entry<String, Object> entry : itemList.entrySet()) {
                                    Log.v("itemlist", entry.getKey());
                                    String key = entry.getKey();
                                    Object value = entry.getValue();

                                    // now work with key and value...
                                    mItemsReference.child(itemName).child(key).setValue(value);
                                    mItemsReference.child(itemName).child(key).setPriority(count++);
                                }


                                mCountReference.child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists()){
                                            mCountReference.child("count").setValue(0);
                                        }

                                        mItemsReference.child(itemName).setPriority(dataSnapshot.getValue());

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                HashMap<String, Object> transaction = new LinkedHashMap<>();
                                transaction.put("timestamp", ServerValue.TIMESTAMP);
                                Log.v("Add Item", price + " " + quantity);
                                transaction.put("amount", price * quantity);
                                transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                transaction.put("quantity",quantity);

                                mTransactionReference.child(itemName).child("outflow").push().setValue(transaction);

                                //set Total Transaction value
                                mTransactionReference.child(itemName).child("total_outflow").setValue((price*quantity));
                                mTransactionReference.child(itemName).child("total_inflow").setValue(0);



                                Toast.makeText(getApplicationContext(), "Product Added", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    mCountReference.child("count").runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            long count = (Long)mutableData.getValue();

                            count++;
                            mutableData.setValue(count);


                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        }
                    });

                    //finish(); //allow user to add another item
                    for (Map.Entry<TextInputEditText, String> entry : fields.entrySet()) {

                        TextInputLayout parent = (TextInputLayout) entry.getKey().getParent().getParent();
                        entry.getKey().setText("");
                        parent.setErrorEnabled(false);
                    }
                    fields.entrySet().iterator().next().getKey().requestFocus();

                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "No Network", Toast.LENGTH_SHORT).show();
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
            /*switch ((view.getInputType())) {
                case InputType.TYPE_CLASS_NUMBER:
                    validateNumberField(view);
                    break;

                case InputType.TYPE_CLASS_TEXT:
                    validateTextField(view);
                    break;
            }*/
        }
    }
}
