package com.example.android.inventorymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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

import com.example.android.inventorymanager.Models.InventoryListItem;
import com.example.android.inventorymanager.Models.ItemDetail;
import com.example.android.inventorymanager.Utilities.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import static android.text.InputType.TYPE_CLASS_TEXT;
import static com.example.android.inventorymanager.MainActivity.RC_CREATE_SCHEMA;

public class InventoryItemDetail extends AppCompatActivity implements View.OnClickListener{

    private String itemId;
    private String businessName;

    private LinearLayout mParent;
    private RecyclerView mDetails;
    private ArrayList<ItemDetail> mList;

    private Button mSellItem;
    private Button mAddItem;

    private int price,quantity;
    private String itemName;

    private Toast mToast;

    private ItemDetailAdapter arrayAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemReference;
    private DatabaseReference mTransactionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_detail);

        //get Database
        mFirebaseDatabase = Utils.getDatabase();


        mParent = (LinearLayout) findViewById(R.id.activity_inventory_item_detail);
        mDetails = (RecyclerView) findViewById(R.id.rv_item_detail);


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


        mList = getFieldValues();
        arrayAdapter = new ItemDetailAdapter(mList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mDetails.getContext(),
                DividerItemDecoration.VERTICAL);

        mDetails.addItemDecoration(dividerItemDecoration);
        mDetails.setLayoutManager(mLayoutManager);
        mDetails.setItemAnimator(new DefaultItemAnimator());
        mDetails.setAdapter(arrayAdapter);



        mItemReference.child("Quantity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    ItemDetail i = mList.get(2);
                    i.value = Long.toString((Long)dataSnapshot.getValue());
                    arrayAdapter.notifyDataSetChanged();
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

    private ArrayList<ItemDetail> getFieldValues(){

        final ArrayList<ItemDetail> list = new ArrayList<>();
        /*if(!mList.isEmpty())
            mList.clear();*/
        mItemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    ItemDetail i = new ItemDetail();
                    i.name = snapshot.getKey();
                    i.value = snapshot.getValue().toString();

                    if(i.name.equals("CostPrice"))
                        price = Integer.parseInt(i.value);
                    else if(i.name.equals("Quantity"))
                        quantity = Integer.parseInt(i.value);

                    Log.v("ItemDetail",i.name+" "+i.value);
                    list.add(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return list;
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

                                mTransactionReference.child("total_inflow").runTransaction(new Transaction.Handler() {
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

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setPositiveButton("Ok",null)
                .setNegativeButton("Cancel",null)
                .create();
        alertDialog.setTitle("Add Unit");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(input.getText().toString().length()==0){
                            Toast.makeText(InventoryItemDetail.this,"Enter quantity",Toast.LENGTH_SHORT).show();
                        }
                        else{

                            final int p = price;
                            final int qty = Integer.parseInt(input.getText().toString());

                            //write transaction in outflow
                            HashMap<String, Object> transaction = new LinkedHashMap<>();
                            transaction.put("timestamp", ServerValue.TIMESTAMP);
                            transaction.put("amount", p * qty);
                            transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            mTransactionReference.child("outflow").push().setValue(transaction);

                            mTransactionReference.child("total_outflow").runTransaction(new Transaction.Handler() {
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



                            //hardcoding the retrieval of quantity field since always going to be 3rd field in the list
                            final ItemDetail quantity_text_view = mList.get(2);
                            //reduce quantity
                            mItemReference.child("Quantity").runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    long q = (Long)mutableData.getValue();
                                    //priority = (Long)mutableData.getPriority();


                                    q = q + qty;
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


                            alertDialog.dismiss();

                        }
                    }
                });
            }
        });
        alertDialog.show();

    }
}
