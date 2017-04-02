package com.example.android.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.inventorymanager.Utilities.Utils;

public class VisualizationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mMostInStock;
    private Button mMostProfitable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        mMostInStock = (Button) findViewById(R.id.bt_most_in_stock);
        mMostProfitable = (Button) findViewById(R.id.bt_most_profitable);

        mMostProfitable.setOnClickListener(this);
        mMostInStock.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,PieChartActivity.class);

        if (Utils.isOnline()) {
            switch (v.getId()) {
                case R.id.bt_most_in_stock:
                    intent.putExtra("choice", 1);
                    Log.v("PieChart","1");
                    startActivity(intent);
                    break;

                case R.id.bt_most_profitable:
                    intent.putExtra("choice", 2);
                    Log.v("PieChart","2");
                    startActivity(intent);
                    break;

            }
        }
        else
        {
            Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show();
        }
    }
}
