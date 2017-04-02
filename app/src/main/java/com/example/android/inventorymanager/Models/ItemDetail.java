package com.example.android.inventorymanager.Models;

/**
 * Created by niyamshah on 20/03/17.
 */

public class ItemDetail implements Comparable<ItemDetail>{
    public String name;
    public String value;

    public ItemDetail()
    {

    }

    public ItemDetail(String n,String v)
    {
        name = n;
        value = v;
    }

    public int compareTo(ItemDetail itemDetail){
        //desceneding order
        return (Integer.parseInt(itemDetail.value) - Integer.parseInt(value));
    }

}
