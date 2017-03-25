package com.example.android.inventorymanager.Models;

/**
 * Model definition for inventory item list recyclerview
 */

public class InventoryListItem {

    public static final String PRICE_FIELD="CostPrice";
    public static final String QUANTITY_FIELD="Quantity";
    public static final String NAME_FIELD="Name";

    public String id;
    public String Name;
    public Integer Quantity;
    public Integer CostPrice;

    public InventoryListItem(){

    }

    public InventoryListItem(String id,String name,Integer qty,Integer price){

        this.Name = name;
        this.Quantity = qty;
        this.CostPrice = price;
    }

    /*public String getItemName(){
        return Name;
    }

    public Integer getQuantity(){
        return Quantity;
    }

    public Integer getPrice(){
        return Price;
    }*/
}
