package com.example.android.inventorymanager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.TransactionDetail;

import java.util.ArrayList;

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
        TransactionDetail inflow = mList.get(position);
        holder.tv1.setText("Selling Price:"+inflow.amount);
        holder.tv2.setText("Sold By:"+inflow.user);
        holder.tv3.setText("Trans Date:"+inflow.timestamp);
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
