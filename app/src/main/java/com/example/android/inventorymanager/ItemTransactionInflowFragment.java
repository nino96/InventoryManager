package com.example.android.inventorymanager;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.inventorymanager.Models.ItemDetail;
import com.example.android.inventorymanager.Models.TransactionDetail;
import com.example.android.inventorymanager.Models.User;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemTransactionInflowFragment extends Fragment {

    private TransactionDetail i = new TransactionDetail();
    private String itemId;
    private String businessName;

    private LinearLayout mParent;
    private RecyclerView mDetails;
    private ArrayList<TransactionDetail> mList = new ArrayList<>();

    private Button mSellItem;
    private Button mAddItem;

    private int price,quantity;
    private String itemName;

    private Toast mToast;

    private InflowDetailAdapter arrayAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemReference;
    private DatabaseReference mTransactionReference;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    public ItemTransactionInflowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_transaction_inflow, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFirebaseDatabase = Utils.getDatabase();
        View v= getView();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        Log.v("Inventory",pref.getString("businessName","null"));
        businessName = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("businessName",null);
        itemName = getActivity().getIntent().getExtras().getString("itemName");
        mTransactionReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("transactions").child(itemName).child("inflow");
        getFieldValues();
        arrayAdapter = new InflowDetailAdapter(mList);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.rv_inflow_detail);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(arrayAdapter);
    }

    private ArrayList<TransactionDetail> getFieldValues(){

        final ArrayList<TransactionDetail> list = new ArrayList<>();
        /*if(!mList.isEmpty())
            mList.clear();*/
        mTransactionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){

                    //final DataSnapshot snap = snapshot;
                    /*for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        String blah = snapshot1.getKey();
                        String ans = snapshot1.getValue().toString();
                        //i.value = snapshot.getValue().toString();
                        if(blah.equals("user")) {
                            DatabaseReference userRef = mFirebaseDatabase.getReference().child("users").child(ans);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    User blah2 = dataSnapshot1.getValue(User.class);
                                    i.user = blah2.username;
                                    Log.v("UserName",i.user);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.v("Failure","Fails");
                                }
                            });

                        }
                        else if(blah.equals("amount"))
                            i.amount = Integer.parseInt(ans);
                        else if(blah.equals("timestamp"))
                            i.timestamp = Long.parseLong(ans);

                        Log.v("InflowDetail",i.user+" "+i.amount+" "+i.timestamp);

                    }*/
                    String user = snapshot.child("user").getValue().toString();
                    Long amount = (Long)snapshot.child("amount").getValue();
                    Long timestamp = (Long)snapshot.child("timestamp").getValue();

                    TransactionDetail item = new TransactionDetail();

                    item.user = user;
                    item.amount = amount;
                    item.timestamp = timestamp;

                    //Log.v("InflowDetail",item.user+" "+item.amount+" "+item.timestamp);


                    mList.add(item);
                    arrayAdapter.notifyDataSetChanged();


                    /*Log.v("InflowFragment",user);

                    DatabaseReference userRef = mFirebaseDatabase.getReference().child("users").child(user);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {

                            TransactionDetail item = new TransactionDetail();




                            item.user = dataSnapshot1.child("username").getValue().toString();
                            Log.v("UserName",item.user);

                            item.amount = amount;
                            item.timestamp = timestamp;

                            //Log.v("InflowDetail",item.user+" "+item.amount+" "+item.timestamp);

                            list.add(item);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.v("Failure","Fails");
                        }
                    });*/






                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return list;
    }
}
