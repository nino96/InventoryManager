package com.example.android.inventorymanager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;
    public static final int RC_CREATE_SCHEMA = 2;

    public static final String SCHEMA_ENTRY_NAME = "fieldName";
    public static final String SCHEMA_ENTRY_TYPE = "fieldType";
    public static final String SCHEMA_ENTRY_REQUIRED = "required";

    static boolean calledAlready = false;

    //the name of business(es) created, not to be confused with businessName which holds selected business in Join Business dialog
    private String businessname;

    private FirebaseDatabase mFireBaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mBusinessesReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static Toast mToast;
    private EditText mBusinessName;
    private Button mCreateBusinessButton;
    private Button mJoinBusinessButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //call to setpersistence must be made before any other usage of FireBaseDatabase instance, therefore in Utils.java

        mFireBaseDatabase = Utils.getDatabase();


        mFireBaseAuth = FirebaseAuth.getInstance();
        mUsersDatabaseReference = mFireBaseDatabase.getReference().child("users");
        mBusinessesReference = mFireBaseDatabase.getReference().child("businesses");

        mUsersDatabaseReference.keepSynced(true);
        mBusinessesReference.keepSynced(true);

        mCreateBusinessButton = (Button)findViewById(R.id.bt_new_business);
        mJoinBusinessButton = (Button)findViewById(R.id.bt_join_business);

        mCreateBusinessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter business name");

                final EditText input = new EditText(MainActivity.this);
                builder.setView(input);

                /*final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(input)
                        .setTitle("Enter business name")
                        .setPositiveButton("OK",null)
                        .setNegativeButton("Cancel",null)
                        .create();*/

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        businessname = input.getText().toString();
                        //final DialogInterface workaround = dialog;              //need to be final to reference in inner class

                        if (businessname.length() < 5) {
                            if(mToast!=null)
                                mToast.cancel();

                            mToast = Toast.makeText(MainActivity.this, "Business name minimum 5 characters", Toast.LENGTH_LONG);
                            mToast.show();

                        } else {
                            mBusinessesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    if (user!=null && !dataSnapshot.hasChild(businessname) ){


                                        //start SchemaInput activity for result and if result is OK then create user and business
                                        //mappings in onAcitivtyResult




                                        Intent intent = new Intent(MainActivity.this,SchemaInput.class);
                                        intent.putExtra("businessName",businessname);
                                        startActivityForResult(intent,RC_CREATE_SCHEMA);

                                    }
                                    else{
                                        if(mToast!=null)
                                            mToast.cancel();

                                        mToast = Toast.makeText(MainActivity.this,"Business Name already taken!",Toast.LENGTH_LONG);
                                        mToast.show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("DatabaseError","database error");
                                }
                            });
                        }

                    }
                });


                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                builder.show();


            }
        });


        mJoinBusinessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null) {
                    final DatabaseReference businesses = mUsersDatabaseReference.child(user.getUid()).child("businesses");

                    if (businesses != null) {
                        //Start the progress dialog
                        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "",
                                "Loading. Please wait...", true);
                        progressDialog.setCancelable(true);


                        final AlertDialog.Builder listDialog = new AlertDialog.Builder(MainActivity.this);
                        listDialog.setTitle("Select Business");


                        final ArrayAdapter<String> listBusinesses = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        businesses.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    listBusinesses.add(snapshot.getKey());
                                }

                                if (listBusinesses.getCount() != 0) {


                                    listDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    listDialog.setAdapter(listBusinesses, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //not to be confused with businessname, which is holds name of last business created and it is
                                            //global to this activity
                                            String businessName = listBusinesses.getItem(which);

                                            //create the schema list from firebase
                                            final List<SchemaEntry> fieldList = new ArrayList<SchemaEntry>();
                                            mBusinessesReference.child(businessName).child("schema").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot fields : dataSnapshot.getChildren()){

                                                        HashMap<String,String> schemaField = (HashMap<String, String>) fields.getValue();
                                                        String name = schemaField.get(SCHEMA_ENTRY_NAME);
                                                        String type = schemaField.get(SCHEMA_ENTRY_TYPE);
                                                        String required = schemaField.get(SCHEMA_ENTRY_REQUIRED);

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


                                            //putting into shared preferences, which makes Intent putExtra redundant
                                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor edit = pref.edit();
                                            Log.v("MainAct",businessName);
                                            edit.putString("businessName",businessName);

                                            edit.commit();

                                            Intent businessHome = new Intent(MainActivity.this, BusinessHome.class);
                                            businessHome.putExtra("businessName", businessName);
                                            startActivity(businessHome);
                                            finish();

                                            if (mToast != null) {
                                                mToast.cancel();
                                            }

                                            mToast = Toast.makeText(MainActivity.this, businessName, Toast.LENGTH_SHORT);
                                            mToast.show();
                                        }
                                    });

                                    progressDialog.dismiss();
                                    listDialog.show();

                                } else {
                                    progressDialog.dismiss();
                                    if (mToast != null) {
                                        mToast.cancel();
                                    }

                                    mToast = Toast.makeText(MainActivity.this, "You are not part of any business", Toast.LENGTH_LONG);
                                    mToast.show();
                                }


                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }


                        });
                    }else{
                        if (mToast != null) {
                            mToast.cancel();
                        }

                        mToast = Toast.makeText(MainActivity.this, "You are not part of any business", Toast.LENGTH_LONG);
                        mToast.show();
                    }

                }
            }
        });



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    //onSignedInInitialize(user.getDisplayName());
                }
                else{
                    //onSignedOutCleanUp();
                    startActivityForResult(
                            new Intent(MainActivity.this, LoginActivity.class),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFireBaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAuthStateListener!=null){
            mFireBaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                //GoogleSignInAccount acct = result.getSignInAccount(); //null
                //String username = acct.getDisplayName();
                //String email = acct.getEmail();
                mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user!=null && !dataSnapshot.hasChild(user.getUid())){
                            mUsersDatabaseReference.child(user.getUid()).setValue(new User(user.getDisplayName(),user.getEmail()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                if(mToast!=null)
                    mToast.cancel();

                mToast = Toast.makeText(this,"Signed in",Toast.LENGTH_SHORT);
                mToast.show();
            }

            if(resultCode == RESULT_CANCELED){
                if(mToast!=null)
                    mToast.cancel();

                mToast = Toast.makeText(this, "Login canceled", Toast.LENGTH_SHORT);
                mToast.show();

                finish();
            }

        }
        else if(requestCode == RC_CREATE_SCHEMA){
            if(resultCode == RESULT_OK){

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null) {
                    mBusinessesReference.child(businessname).child("owner").setValue(user.getUid());
                    mBusinessesReference.child(businessname).child("members").child(user.getUid()).setValue(true);
                    mUsersDatabaseReference.child(user.getUid()).child("businesses").child(businessname).child("owner").setValue(true);

                    if(mToast!=null)
                        mToast.cancel();

                    mToast = Toast.makeText(MainActivity.this,"Business created successfully",Toast.LENGTH_LONG);
                    mToast.show();
                }
                else{
                    //remove any data that may have been created in SchemaInput activity
                    if(mBusinessesReference.child(businessname)!=null){
                        mBusinessesReference.child(businessname).setValue(null);
                    }

                    if(mToast!=null)
                        mToast.cancel();

                    mToast = Toast.makeText(MainActivity.this,"Business could not be created",Toast.LENGTH_LONG);
                    mToast.show();
                }



            }

            if(resultCode == RESULT_CANCELED){

                //remove any data that may have been created in SchemaInput activity
                //no changes made to users, so no need to check for that
                if(mBusinessesReference.child(businessname)!=null){
                    mBusinessesReference.child(businessname).setValue(null);
                }

                if(mToast!=null)
                    mToast.cancel();

                mToast = Toast.makeText(MainActivity.this,"Business could not be created",Toast.LENGTH_LONG);
                mToast.show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch(itemId){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /*private void onSignedInInitialize(String username){
        //mUsername = username;
        //attachDatabaseListener();
    }

    private void onSignedOutCleanUp(){
        //mUsername = ANONYMOUS;
        //mMessageAdapter.clear();
        //detachDatabaseListener();
    }*/
}
