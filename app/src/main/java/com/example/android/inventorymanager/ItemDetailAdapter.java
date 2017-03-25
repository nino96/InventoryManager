package com.example.android.inventorymanager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.ItemDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niyamshah on 24/03/17.
 */

public class ItemDetailAdapter extends RecyclerView.Adapter<ItemDetailAdapter.ItemDetailViewHolder> {
    private ArrayList<ItemDetail> mList;

    public ItemDetailAdapter(ArrayList<ItemDetail> list){
        mList = list;
    }

    @Override
    public ItemDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2,parent,false);

        return new ItemDetailViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(ItemDetailViewHolder holder, int position) {
        ItemDetail item = mList.get(position);
        holder.bindInventoryListItem(item);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ItemDetailViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Context mContext;
        TextView tv1,tv2;



        public ItemDetailViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            tv1 = (TextView) itemView.findViewById(android.R.id.text1);
            tv2 = (TextView) itemView.findViewById(android.R.id.text2);

        }

        public void bindInventoryListItem(ItemDetail item){

            tv1.setText(item.name);
            tv1.setTypeface(null,Typeface.BOLD);
            tv2.setText(item.value);
        }
    }

}
