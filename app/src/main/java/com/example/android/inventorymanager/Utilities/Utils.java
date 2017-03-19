package com.example.android.inventorymanager.Utilities;

import android.util.Log;

import com.example.android.inventorymanager.MainActivity;
import com.example.android.inventorymanager.Models.SchemaEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
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

    public static void getSchemaEntryList(DatabaseReference mBusinessesReference,String businessName){
        //create the schema list from firebase
        final List<SchemaEntry> fieldList = new ArrayList<SchemaEntry>();
        mBusinessesReference.child(businessName).child("schema").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot fields : dataSnapshot.getChildren()){

                    HashMap<String,String> schemaField = (HashMap<String, String>) fields.getValue();
                    String name = schemaField.get(MainActivity.SCHEMA_ENTRY_NAME);
                    String type = schemaField.get(MainActivity.SCHEMA_ENTRY_TYPE);
                    String required = schemaField.get(MainActivity.SCHEMA_ENTRY_REQUIRED);

                    Log.v("Schema List",name+" "+type+" "+required);

                    if(required.equals("true"))
                        fieldList.add(new SchemaEntry(name,type,true));
                    else
                        fieldList.add(new SchemaEntry(name,type,false));
                }

                //now change system-wide schema list
                schemaEntryList = fieldList;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
