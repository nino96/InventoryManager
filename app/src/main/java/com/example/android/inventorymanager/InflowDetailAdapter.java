package com.example.android.inventorymanager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.TransactionDetail;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by niyamshah on 24/03/17.
 */

public class InflowDetailAdapter extends RecyclerView.Adapter<InflowDetailAdapter.InflowDetailViewHolder> {
    private ArrayList<TransactionDetail> mList;

    public InflowDetailAdapter(ArrayList<TransactionDetail> list){
        mList = list;
    }

    @Override
    public InflowDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_inflow, parent, false);

        return new InflowDetailViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(InflowDetailViewHolder holder, int position) {

        final InflowDetailViewHolder holder_copy = holder;
        final TransactionDetail inflow = mList.get(position);

        DatabaseReference userRef = Utils.getDatabase().getReference().child("users").child(inflow.user);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {

                String username = dataSnapshot1.child("username").getValue().toString();
                holder_copy.tv1.setText("Sell Price : "+Long.toString(inflow.amount));

                //Log.v("InflowAdapter",inflow.user);
                holder_copy.tv2.setText("User : "+username);

                SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");


                holder_copy.tv3.setText("Date : "+sfd.format(new Date(inflow.timestamp)));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Failure","Fails");
            }
        });


        Log.v("InflowAdapter",inflow.amount+"");

        //holder.tv4.setText("Quantity Sold:"+inflow.quantity);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class InflowDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tv1,tv2,tv3,tv4;



        public InflowDetailViewHolder(View View){
            super(View);
            tv1 = (TextView) itemView.findViewById(R.id.tv_selling_price);
            tv2 = (TextView) itemView.findViewById(R.id.tv_seller);
            tv3 = (TextView) itemView.findViewById(R.id.tv_time_sold);
            tv4 = (TextView) itemView.findViewById(R.id.tv_sold_quantity);

        }

    }

}
