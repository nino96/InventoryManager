package com.example.android.inventorymanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SchemaInput extends AppCompatActivity {

    // Parent view for all rows and the add button.
    private LinearLayout mContainerView;
    // The "Add new" button
    private Button mAddButton;
    //The create schema button
    private Button mCreateSchemaButton;


    // There always should be only one empty row, other empty rows will
    // be removed.
    private View mExclusiveEmptyView;

    //The name and type definition of schema entries
    private List<SchemaEntry> fieldList;

    //The business whose schema is being created
    private String businessName;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBusinessDatabaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.row_container);

        mContainerView = (LinearLayout) findViewById(R.id.parentView);
        mAddButton = (Button) findViewById(R.id.btnAddNewItem);
        mCreateSchemaButton = (Button) findViewById(R.id.bt_create_schema);
        //The schema list
        fieldList = new ArrayList<SchemaEntry>();

        businessName = getIntent().getExtras().getString("businessName");
        mFirebaseDatabase = Utils.getDatabase();
        mBusinessDatabaseReference = mFirebaseDatabase.getReference("businesses").child(businessName);


        // Add some examples
        LinearLayout mNecessaryFields = (LinearLayout)findViewById(R.id.necessaryFields);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowNecesary = inflater.inflate(R.layout.row_necessary,null);

        TextView tv = (TextView)rowNecesary.findViewById(R.id.tv_field_name);
        tv.setText("Item Name");
        tv = (TextView)rowNecesary.findViewById(R.id.tv_field_type);
        tv.setText("String");
        mNecessaryFields.addView(rowNecesary);
        fieldList.add(new SchemaEntry("Name",Types.String.name()));

        rowNecesary = inflater.inflate(R.layout.row_necessary,null);
        tv = (TextView)rowNecesary.findViewById(R.id.tv_field_name);
        tv.setText("Avg Price");
        tv = (TextView)rowNecesary.findViewById(R.id.tv_field_type);
        tv.setText("Integer");
        mNecessaryFields.addView(rowNecesary);
        fieldList.add(new SchemaEntry("Price",Types.Integer.name()));

        rowNecesary = inflater.inflate(R.layout.row_necessary,null);
        tv = (TextView)rowNecesary.findViewById(R.id.tv_field_name);
        tv.setText("Quantity");
        tv = (TextView)rowNecesary.findViewById(R.id.tv_field_type);
        tv.setText("Integer");
        mNecessaryFields.addView(rowNecesary);
        fieldList.add(new SchemaEntry("Quantity",Types.Integer.name()));





        //mContainerView.addView(necessaryFields);
        mCreateSchemaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //a confirmation
                new AlertDialog.Builder(SchemaInput.this)
                        .setTitle("Confirm")
                        .setMessage("Do you really want to confirm? You won't be able to change the schema")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                for (int i = 0; i < mContainerView.getChildCount(); i++) {
                                    if (mContainerView.getChildAt(i) instanceof LinearLayout) {

                                        //getChildAt(i) will give inner linear layouts(direct children)
                                        //and not edit texts directly(nested children), so get inner linear layout
                                        //but these could be the ones added by default which have already been added to field list
                                        //so check if EditText is null, since those default ones have textviews
                                        LinearLayout inner = (LinearLayout) mContainerView.getChildAt(i);
                                        EditText et = (EditText) inner.findViewById(R.id.editText);
                                        Spinner sp = (Spinner) inner.findViewById(R.id.spinnerFieldType);

                                        if(et!=null && sp!=null && et.getText().toString().length()>0) {
                                            Log.e(SchemaInput.class.getCanonicalName(),et.getText().toString()+sp.getSelectedItem().toString());
                                            fieldList.add(new SchemaEntry(et.getText().toString(), sp.getSelectedItem().toString()));
                                        }
                                        else{
                                            //Log.e(SchemaInput.class.getCanonicalName(),"Empty String");
                                        }
                                    }
                                    //Log.e(SchemaInput.class.getCanonicalName(),"Not Linear Layout");
                                }

                                mBusinessDatabaseReference.child("schema").setValue(fieldList);
                                //change system wide schema list to the schema just created
                                Utils.schemaEntryList = fieldList;

                                //now go back to the previous activity with success message which will create mappings of user and business
                                //one problem is that business schema already added to database, so what if something goes wrong in this for loop
                                //then business schema will stay but MainAcitivty will get RESULT_CANCELED so mappings won't be create
                                Intent intent = new Intent();
                                setResult(RESULT_OK);
                                finish();

                                //Toast.makeText(SchemaInput.this, "Yaay", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();



            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // TODO: Handle screen rotation:
        // encapsulate information in a parcelable object, and save it
        // into the state bundle.

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // TODO: Handle screen rotation:
        // restore the saved items and inflate each one with inflateEditRow;

    }

    // onClick handler for the "Add new" button;
    public void onAddNewClicked(View v) {
        // Inflate a new row and hide the button self.
        inflateEditRow(null);
        v.setVisibility(View.GONE);
    }

    // onClick handler for the "X" button of each row
    public void onDeleteClicked(View v) {

        // remove the row by calling the getParent on button
        mAddButton.setVisibility(View.VISIBLE);
        mContainerView.removeView((View) v.getParent());
    }

    // Helper for inflating a row
    private void inflateEditRow(String name) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.row, null);
        final ImageButton deleteButton = (ImageButton) rowView
                .findViewById(R.id.buttonDelete);
        final EditText editText = (EditText) rowView
                .findViewById(R.id.editText);
        final Spinner typeSpinner = (Spinner)rowView.findViewById(R.id.spinnerFieldType);

        if (name != null && !name.isEmpty()) {
            editText.setText(name);
        } else {
            mExclusiveEmptyView = rowView;
            //deleteButton.setVisibility(View.INVISIBLE);
        }

        // A TextWatcher to control the visibility of the "Add new" button and
        // handle the exclusive empty view.
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    mAddButton.setVisibility(View.GONE);
                    //deleteButton.setVisibility(View.INVISIBLE);

                    if (mExclusiveEmptyView != null
                            && mExclusiveEmptyView != rowView) {
                        mContainerView.removeView(mExclusiveEmptyView);
                    }
                    mExclusiveEmptyView = rowView;
                } else {

                    if (mExclusiveEmptyView == rowView) {
                        mExclusiveEmptyView = null;
                    }


                    mAddButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        // Inflate at the end of all rows but before the "Add new" button
        mContainerView.addView(rowView, mContainerView.getChildCount() - 1);
    }
}
