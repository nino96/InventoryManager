package com.example.android.inventorymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.InventoryListItem;

import static android.icu.lang.UProperty.INT_START;

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

        //Log.v("ItemDetail",item.id);

        mItemName.setText(item.Name);
        mItemName.setTag(item.id);

        SpannableStringBuilder str = new SpannableStringBuilder("Price - "+item.CostPrice);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mItemPrice.setText(str);

        str = new SpannableStringBuilder("Quantity - "+item.Quantity);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        mItemQuantity.setText(str);

    }




    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(),InventoryItemDetail.class);
        intent.putExtra("itemName",((TextView)v.findViewById(R.id.tv_inventory_list_itemname)).getText().toString());
        v.getContext().startActivity(intent);
    }
}
