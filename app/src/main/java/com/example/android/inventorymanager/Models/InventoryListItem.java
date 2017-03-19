package com.example.android.inventorymanager.Models;

/**
 * Model definition for inventory item list recyclerview
 */

public class InventoryListItem {
    public String Name;
    public Integer Quantity;
    public Integer Price;

    public InventoryListItem(){

    }

    public InventoryListItem(String name,Integer qty,Integer price){
        this.Name = name;
        this.Quantity = qty;
        this.Price = price;
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
