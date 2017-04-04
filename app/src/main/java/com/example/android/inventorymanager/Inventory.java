package com.example.android.inventorymanager;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    private LinearLayoutManager mLayoutManager;

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

        //This breaks sorting functionality, don't know why exactly
        //mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        /*DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                manager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);*/

        mFirebaseAdapter = new FirebaseRecyclerAdapter<InventoryListItem,InventoryItemListViewHolder>(InventoryListItem.class,R.layout.inventory_list_item,InventoryItemListViewHolder.class,mItemsReference) {

            @Override
            protected void populateViewHolder(InventoryItemListViewHolder holder, InventoryListItem item, int position) {

                item.id = getRef(position).getKey();
                //Log.v("Item Detail",item.id);

                holder.bindInventoryListItem(item);
            }
        };
        mRecyclerView.setAdapter(mFirebaseAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inventory_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (Utils.isOnline()) {
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_sort_by_name) {
                mFirebaseAdapter = new FirebaseRecyclerAdapter<InventoryListItem, InventoryItemListViewHolder>(InventoryListItem.class, R.layout.inventory_list_item, InventoryItemListViewHolder.class, mItemsReference.orderByChild("Name")) {

                    @Override
                    protected void populateViewHolder(InventoryItemListViewHolder holder, InventoryListItem item, int position) {

                        item.id = getRef(position).getKey();

                        Log.v("ItemDetail",item.id);

                        holder.bindInventoryListItem(item);
                    }
                };
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setAdapter(mFirebaseAdapter);
                mRecyclerView.setLayoutManager(mLayoutManager);
            } else if (id == R.id.action_sort_by_latest) {
                mFirebaseAdapter = new FirebaseRecyclerAdapter<InventoryListItem, InventoryItemListViewHolder>(InventoryListItem.class, R.layout.inventory_list_item, InventoryItemListViewHolder.class, mItemsReference.orderByPriority()) {

                    @Override
                    protected void populateViewHolder(InventoryItemListViewHolder holder, InventoryListItem item, int position) {

                        item.id = getRef(position).getKey();
                        //Log.v("Item Detail",item.id);

                        holder.bindInventoryListItem(item);
                    }
                };

                mLayoutManager = new LinearLayoutManager(this);
                mLayoutManager.setReverseLayout(true);
                mLayoutManager.setStackFromEnd(true);

                mRecyclerView.swapAdapter(mFirebaseAdapter, false);
                mRecyclerView.setLayoutManager(mLayoutManager);
            } else if (id == R.id.action_sort_by_quantity) {
                mFirebaseAdapter = new FirebaseRecyclerAdapter<InventoryListItem, InventoryItemListViewHolder>(InventoryListItem.class, R.layout.inventory_list_item, InventoryItemListViewHolder.class, mItemsReference.orderByChild("Quantity")) {

                    @Override
                    protected void populateViewHolder(InventoryItemListViewHolder holder, InventoryListItem item, int position) {

                        item.id = getRef(position).getKey();
                        //Log.v("Item Detail",item.id);

                        holder.bindInventoryListItem(item);
                    }
                };
                mLayoutManager = new LinearLayoutManager(this);
                mLayoutManager.setReverseLayout(true);
                mLayoutManager.setStackFromEnd(true);

                mRecyclerView.swapAdapter(mFirebaseAdapter, false);
                mRecyclerView.setLayoutManager(mLayoutManager);

            }




        }
        else{
            Toast.makeText(this,"No Network",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }
}
