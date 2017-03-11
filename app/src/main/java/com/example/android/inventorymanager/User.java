package com.example.android.inventorymanager;

/**
 * Created by niyamshah on 04/03/17.
 */

public class User {
    public String username;
    public String email;

    public User(){

    }

    public User(String username, String email) {
        this.email = email;
        this.username = username;
    }
}
