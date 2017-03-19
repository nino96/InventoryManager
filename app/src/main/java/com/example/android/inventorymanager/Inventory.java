package com.example.android.inventorymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.util.Util;
import com.example.android.inventorymanager.Models.InventoryListItem;
import com.example.android.inventorymanager.Utilities.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Inventory extends AppCompatActivity {

    private String businessName;
    private RecyclerView mRecyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemsReference;
    private FirebaseRecyclerAdapter mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.v("Inventory",pref.getString("businessName","null"));
        businessName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("businessName",null);

        //get database reference
        mFirebaseDatabase = Utils.getDatabase();
        mItemsReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("items");

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_item_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                manager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<InventoryListItem,InventoryItemListViewHolder>(InventoryListItem.class,R.layout.inventory_list_item,InventoryItemListViewHolder.class,mItemsReference) {

            @Override
            protected void populateViewHolder(InventoryItemListViewHolder holder, InventoryListItem item, int position) {
                holder.bindInventoryListItem(item);
            }
        };
        mRecyclerView.setAdapter(mFirebaseAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }
}
