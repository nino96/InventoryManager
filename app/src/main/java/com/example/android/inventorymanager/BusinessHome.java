package com.example.android.inventorymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class BusinessHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private String businessName;
    private Button mAddItemButton;
    private Button mViewInventoryButton;
    private Button mAddMemberButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBusinessesReference;
    private DatabaseReference mUsersReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabase = Utils.getDatabase();
        mUsersReference = mFirebaseDatabase.getReference().child("users");
        mBusinessesReference = mFirebaseDatabase.getReference().child("businesses");

        //Business Name from SharedPreferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Log.v("BusHome",pref.getString("businessName","null"));
        businessName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("businessName",null);
        //Toast.makeText(this, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),Toast.LENGTH_SHORT).show();
        this.setTitle(businessName);


        //create the schema list from firebase
        Utils.getSchemaEntryList(mBusinessesReference,businessName);


        mAddItemButton = (Button) findViewById(R.id.bt_add_item);
        mViewInventoryButton = (Button) findViewById(R.id.bt_view_inventory);
        mAddMemberButton = (Button) findViewById(R.id.bt_add_member);
        Button mVisualizationsButton = (Button) findViewById(R.id.bt_visualizations);

        mAddItemButton.setOnClickListener(this);
        mViewInventoryButton.setOnClickListener(this);
        mAddMemberButton.setOnClickListener(this);
        mVisualizationsButton.setOnClickListener(this);


        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/


    }

    @Override
    public void onClick(View v) {

        if(Utils.isOnline()) {
            switch (v.getId()) {
                case R.id.bt_add_item:
                    Intent intent = new Intent(this, AddItemActivity.class);
                    startActivity(intent);
                    break;

                case R.id.bt_view_inventory:
                    intent = new Intent(this, Inventory.class);
                    startActivity(intent);
                    break;

                case R.id.bt_add_member:
                    AlertDialog.Builder builder = new AlertDialog.Builder(BusinessHome.this);
                    builder.setTitle("Enter member email");

                    final EditText input = new EditText(BusinessHome.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String email = input.getText().toString();
                            //Log.v("BusinessHome",email);

                            if (email.length() > 0) {


                                Query query = mUsersReference.orderByChild("email").equalTo(email);

                                query.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                                        if (dataSnapshot.getValue() != null) {

                                            Log.v("BusinessName",dataSnapshot.getKey());

                                            mUsersReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.hasChild("businesses") || !dataSnapshot.child("businesses").hasChild(businessName)){
                                                        mBusinessesReference.child(businessName).child("members").child(dataSnapshot.getKey()).setValue(true);
                                                        mUsersReference.child(dataSnapshot.getKey()).child("businesses").child(businessName).child("owner").setValue(false);
                                                        Toast.makeText(BusinessHome.this, "Member added successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(BusinessHome.this, "Member already present", Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                    }
                                });

                                //if user doesn't exist
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            Toast.makeText(BusinessHome.this, "No such user", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                Toast.makeText(BusinessHome.this, "Enter user's email address", Toast.LENGTH_SHORT).show();
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

                    break;

                case R.id.bt_visualizations:
                    intent = new Intent(this,VisualizationActivity.class);
                    startActivity(intent);
                    break;


            }
        }
        else{
            Toast.makeText(BusinessHome.this, "No network", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.business_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_exit){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();

            editor.remove("businessName");
            editor.commit();


            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        }  else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
