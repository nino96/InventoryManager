package com.example.android.inventorymanager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.InflowDetail;

import java.util.ArrayList;

/**
 * Created by niyamshah on 24/03/17.
 */

public class InflowDetailAdapter extends RecyclerView.Adapter<InflowDetailAdapter.InflowDetailViewHolder> {
    private ArrayList<InflowDetail> mList;

    public InflowDetailAdapter(ArrayList<InflowDetail> list){
        mList = list;
    }

    @Override
    public InflowDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2,parent,false);

        return new InflowDetailViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(InflowDetailViewHolder holder, int position) {
        InflowDetail inflow = mList.get(position);
        holder.bindInflowListItem(inflow);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class InflowDetailViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Context mContext;
        TextView tv1,tv2;



        public InflowDetailViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            tv1 = (TextView) itemView.findViewById(android.R.id.text1);
            tv2 = (TextView) itemView.findViewById(android.R.id.text2);

        }

        public void bindInflowListItem(InflowDetail item){

            tv1.setText(item.name);
            tv1.setTypeface(null,Typeface.BOLD);
            tv2.setText(item.amt);
        }
    }

}
