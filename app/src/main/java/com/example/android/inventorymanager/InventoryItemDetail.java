package com.example.android.inventorymanager;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.ItemDetail;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemDetail extends AppCompatActivity {

    private String itemId;
    private String businessName;

    private LinearLayout mParent;
    private ListView mDetails;
    private List<ItemDetail> mList;
    private ArrayAdapter<ItemDetail> arrayAdapter;

    private DatabaseReference itemReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_detail);

        mParent = (LinearLayout) findViewById(R.id.activity_inventory_item_detail);
        mDetails = (ListView) findViewById(R.id.lv_item_detail);
        mList = new ArrayList<ItemDetail>();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Log.v("Inventory",pref.getString("businessName","null"));
        businessName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("businessName",null);

        itemId = getIntent().getExtras().getString("id");
        //Log.v("Item Detail",itemId);

        TextView tv = new TextView(this);

        itemReference = Utils.getDatabase().getReference().child("businesses").child(businessName).child("items").child(itemId);
        itemReference.keepSynced(true);

        itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    ItemDetail i = new ItemDetail();
                    i.name = snapshot.getKey();
                    i.value = snapshot.getValue().toString();

                    //Log.v("ItemDetail",i.name+" "+i.value);
                    mList.add(i);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        arrayAdapter = new ArrayAdapter<ItemDetail>(InventoryItemDetail.this, android.R.layout.simple_list_item_2, android.R.id.text1, mList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                //Log.v("ItemDetail1",mList.get(position).name+" "+mList.get(position).value);

                text1.setText(mList.get(position).name);
                text1.setTypeface(null, Typeface.BOLD);
                text2.setText(mList.get(position).value);
                return view;
            }
        };

        mDetails.setAdapter(arrayAdapter);

    }
}
