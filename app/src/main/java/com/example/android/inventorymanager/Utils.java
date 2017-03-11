package com.example.android.inventorymanager;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by niyamshah on 06/03/17.
 */

public class Utils {
    private static FirebaseDatabase mDatabase;
    public static List<SchemaEntry> schemaEntryList;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
}
