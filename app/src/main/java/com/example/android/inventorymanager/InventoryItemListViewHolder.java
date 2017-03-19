package com.example.android.inventorymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.InventoryListItem;

/**
 * Created by niyamshah on 19/03/17.
 */

public class InventoryItemListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    Context mContext;



    public InventoryItemListViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);

    }

    public void bindInventoryListItem(InventoryListItem item){
        TextView mItemName = (TextView) mView.findViewById(R.id.tv_inventory_list_itemname);
        TextView mItemPrice = (TextView) mView.findViewById(R.id.tv_inventory_list_itemprice);
        TextView mItemQuantity = (TextView) mView.findViewById(R.id.tv_inventory_list_itemquantity);


        mItemName.setText(item.Name);
        mItemName.setTag(item.id);
        mItemPrice.setText("Price - "+item.Price);
        mItemQuantity.setText("Qty - "+item.Quantity);

    }




    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(),InventoryItemDetail.class);
        intent.putExtra("id",(String)v.findViewById(R.id.tv_inventory_list_itemname).getTag());
        v.getContext().startActivity(intent);
    }
}
