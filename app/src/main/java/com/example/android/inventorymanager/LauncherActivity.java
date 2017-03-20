package com.example.android.inventorymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.inventorymanager.Models.SchemaEntry;
import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {

    private DatabaseReference mBusinessesReference;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_launcher);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefs.getString("businessName",null)!=null){

            mBusinessesReference = Utils.getDatabase().getReference().child("businesses");

            String businessName = prefs.getString("businessName",null);
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
                    Utils.schemaEntryList = fieldList;
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //Log.v("Launcher",prefs.getString("businessName","null"));
            Intent intent = new Intent(this,BusinessHome.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Log.v("Launcher","fishy");
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
