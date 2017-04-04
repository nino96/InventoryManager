package com.example.android.inventorymanager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.android.inventorymanager.Models.ItemDetail;
import com.example.android.inventorymanager.Utilities.Utils;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class PieChartActivity extends AppCompatActivity {
    private TextView mChartTitle;
    private int choice;

    private PieChart mPieChart;
    private ArrayList<PieEntry> yValues;

    private String businessName;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemsReference;
    private DatabaseReference mTransactionReference;

    public static  final int[] MY_COLORS = {
            Color.rgb(84,124,101), Color.rgb(64,64,64), Color.rgb(153,19,0),
            Color.rgb(38,40,53), Color.rgb(215,60,55)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        businessName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("businessName",null);

        choice = getIntent().getExtras().getInt("choice");

        mPieChart = (PieChart) findViewById(R.id.pc_piechart);
        mChartTitle = (TextView) findViewById(R.id.tv_chart_title);

        mFirebaseDatabase = Utils.getDatabase();
        mItemsReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("items");
        mTransactionReference = mFirebaseDatabase.getReference().child("businesses").child(businessName).child("transactions");

        Description desc = new Description();
        desc.setText("");
        mPieChart.setDescription(desc);

        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });

        setDataValues();

    }

    private void setDataValues()
    {

        if (choice == 1) {
            mItemsReference.orderByChild("Quantity").limitToLast(5).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    yValues = new ArrayList<>();

                    ArrayList<ItemDetail> list = new ArrayList<ItemDetail>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        int value = Integer.parseInt(snapshot.child("Quantity").getValue().toString());
                        yValues.add(new PieEntry(value, snapshot.getKey()));
                    }
                    mChartTitle.setText("Top 5 in Stock");

                    createPieChart();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if(choice==2){
            mTransactionReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //need to clear arraylist inside the listener, since outside code not executed on ValueEvent
                    yValues = new ArrayList<>();

                    ArrayList<ItemDetail> list = new ArrayList<ItemDetail>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        int inflow = Integer.parseInt(snapshot.child("total_inflow").getValue().toString());
                        //long outflow = (Long) snapshot.child("total_outflow").getValue();
                        //long profit = inflow-outflow;

                        if(inflow>0)
                            list.add(new ItemDetail(snapshot.getKey(),Integer.toString(inflow)));

                    }
                    mChartTitle.setText("Top Selling");
                    Collections.sort(list);

                    for(int i = 0;i<5;i++)
                    {
                        if(i<list.size()) {
                            yValues.add(new PieEntry(Integer.parseInt(list.get(i).value), list.get(i).name));
                        }
                        else{
                            break;
                        }
                    }
                    createPieChart();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void createPieChart()
    {
        // create pieDataSet
        if(yValues.size()==0){
            mPieChart.clear();

            mPieChart.setNoDataText("No items/transactions");
            Paint p = mPieChart.getPaint(Chart.PAINT_INFO);
            p.setTextSize(40f);
            p.setColor(Color.BLACK);
            mPieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        // adding colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // Added My Own colors
        for (int c : MY_COLORS)
            colors.add(c);


        dataSet.setColors(colors);

        //  create pie data object and set xValues and yValues and set it to the pieChart
        PieData data = new PieData(dataSet);
        //   data.setValueFormatter(new DefaultValueFormatter());
        //   data.setValueFormatter(new PercentFormatter());

        data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        mPieChart.setDrawHoleEnabled(false);
        Description desc = mPieChart.getDescription();
        desc.setTextSize(12f);

        Legend l = mPieChart.getLegend();
        l.setEnabled(false);
        //mPieChart.getLegend().setWordWrapEnabled(true);


        mPieChart.setData(data);
        // undo all highlights
        mPieChart.highlightValues(null);

        // refresh/update pie chart
        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();

        // animate piechart
        mPieChart.animateXY(1400, 1400);
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal if needed
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            //if(value<0)
            //  return "-" + mFormat.format(value)+"";
            if(choice==2)
                return mFormat.format(value) + "\u20B9"; // e.g. append a rupee-sign
            else
                return mFormat.format(value) + "";
        }
    }
}
