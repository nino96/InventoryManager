package com.example.android.inventorymanager;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.inventorymanager.Utilities.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemTransactionInflowFragment extends Fragment {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemReference;
    private DatabaseReference mTransactionReference;

    public ItemTransactionInflowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_transaction_inflow, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFirebaseDatabase = Utils.getDatabase();

        View v= getView();

    }
}
