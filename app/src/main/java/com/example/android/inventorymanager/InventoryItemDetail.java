package com.example.android.inventorymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymanager.Models.ItemDetail;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.R.id.input;
import static com.example.android.inventorymanager.MainActivity.RC_CREATE_SCHEMA;

public class InventoryItemDetail extends AppCompatActivity implements View.OnClickListener{

    private String itemId;
    private String businessName;

    private LinearLayout mParent;
    private ListView mDetails;
    private List<ItemDetail> mList;
    private ArrayAdapter<ItemDetail> arrayAdapter;

    private Button mSellItem;
    private Button mAddItem;

    private int price,quantity;
    private String itemName;

    private Toast mToast;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemReference;
    private DatabaseReference mTransactionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_detail);

        //get Database
        mFirebaseDatabase = Utils.getDatabase();

        arrayAdapter = null;
        mParent = (LinearLayout) findViewById(R.id.activity_inventory_item_detail);
        mDetails = (ListView) findViewById(R.id.lv_item_detail);
        mList = new ArrayList<ItemDetail>();


        mSellItem = (Button) findViewById(R.id.bt_sell_unit);
        mAddItem = (Button) findViewById(R.id.bt_add_unit);
        mSellItem.setOnClickListener(this);
        mAddItem.setOnClickListener(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.v("Inventory",pref.getString("businessName","null"));
        businessName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("businessName",null);
        itemName = getIntent().getExtras().getString("itemName");

        //get required FirebaseDatabase references
        mItemReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("items").child(itemName);
        mItemReference.keepSynced(true);
        mTransactionReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("transactions").child(itemName);

        //populate list
        getFieldValues();
        arrayAdapter = new ArrayAdapter<ItemDetail>(InventoryItemDetail.this, android.R.layout.simple_list_item_2, android.R.id.text1, mList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(mList.get(position).name);
                text1.setTypeface(null, Typeface.BOLD);
                text2.setText(mList.get(position).value);
                return view;
            }
        };
        mDetails.setAdapter(arrayAdapter);

    }


    private void getFieldValues(){

        if(!mList.isEmpty())
            mList.clear();
        mItemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    ItemDetail i = new ItemDetail();
                    i.name = snapshot.getKey();
                    i.value = snapshot.getValue().toString();

                    if(i.name.equals("Price"))
                        price = Integer.parseInt(i.value);
                    else if(i.name.equals("Quantity"))
                        quantity = Integer.parseInt(i.value);

                    Log.v("ItemDetail",i.name+" "+i.value);
                    mList.add(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View v) {

        if (Utils.isOnline()) {
            switch (v.getId()) {
                case R.id.bt_sell_unit:
                    getSoldUnits();
                    break;

                case R.id.bt_add_unit:
                    getBoughtUnits();

                    break;

            }
        }
        else{
            Toast.makeText(this,"No Network",Toast.LENGTH_SHORT).show();
        }
    }


    private void getSoldUnits(){

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setPositiveButton("Ok",null)
                .setNegativeButton("Cancel",null)
                .create();
        alertDialog.setTitle("Sell Unit");

        View dialog_layout = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        // Create the text field in the alert dialog...
        final EditText text1 = (EditText) dialog_layout.findViewById(R.id.text1);
        final EditText text2 = (EditText) dialog_layout.findViewById(R.id.text2);

        alertDialog.setView(dialog_layout);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {


                        if(text1.getText().toString().length()==0 || text2.getText().toString().length()==0){
                            Toast.makeText(InventoryItemDetail.this,"Enter both fields",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            final int p = Integer.parseInt(text1.getText().toString());
                            final int qty = Integer.parseInt(text2.getText().toString());

                            Log.v("ItemDetail",p+" "+qty);

                            if(quantity==0){
                                Toast.makeText(InventoryItemDetail.this, "No Item to sell, Add Item", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                            else if(p<=0 || qty<=0) {
                                Toast.makeText(InventoryItemDetail.this, "Enter non zero value", Toast.LENGTH_SHORT).show();
                            }
                            else if(qty>quantity) {
                                Toast.makeText(InventoryItemDetail.this, "Enter quantity <= " + quantity, Toast.LENGTH_SHORT).show();
                            }
                            else{

                                //create inflow transaction
                                HashMap<String, Object> transaction = new LinkedHashMap<>();
                                transaction.put("timestamp", ServerValue.TIMESTAMP);
                                transaction.put("amount", p * qty);
                                transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mTransactionReference.child("inflow").push().setValue(transaction);

                                mTransactionReference.child("total").runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        long sum = (Long)mutableData.getValue();

                                        sum = sum + (p*qty);
                                        mutableData.setValue(sum);

                                        Log.v("Total",sum+"");
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        if(mToast!=null)
                                            mToast.cancel();

                                        mToast = Toast.makeText(InventoryItemDetail.this,"Successful",Toast.LENGTH_SHORT);
                                        mToast.show();

                                    }
                                });


                                //hardcoding the retrieval of quantity field since always going to be 3rd field
                                final ItemDetail quantity_text_view = mList.get(2);
                                //reduce quantity
                                mItemReference.child("Quantity").runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        long q = (Long)mutableData.getValue();
                                        //priority = (Long)mutableData.getPriority();


                                        q = q - qty;
                                        mutableData.setValue(q);
                                        quantity_text_view.value = Long.toString(q);
                                        //mutableData.setPriority(priority);

                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        //Toast.makeText(InventoryItemDetail.this,"Successful",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //if all sold then remove item from database or maybe not
                                //hardcoding setPriority since setPriority deletes priority when done on mutableData inside
                                //the above handler
                                mItemReference.child("Quantity").setPriority(2);

                                //update the member variable quantity
                                mList.set(2,quantity_text_view);
                                quantity = Integer.parseInt(mList.get(2).value);
                                Log.v("Quantity",quantity+"");

                                alertDialog.dismiss();

                            }

                        }
                    }
                });
            }
        });

        alertDialog.show();
    }


    private void getBoughtUnits(){
        AlertDialog dialog;
    }
}
