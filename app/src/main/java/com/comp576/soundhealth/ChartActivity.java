package com.comp576.soundhealth;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.view.ColumnChartView;

public class ChartActivity extends AppCompatActivity {
    private PieChartView pieChartView;
    private ColumnChartView barChartView;
    private ArrayAdapter<Data> adapter;
    private List<Data> data = new ArrayList<>();
    private ListView dataListView;
    private DataRepository dataRepository;
    private Boolean dataQueried = false;
    private float thirties, fourties, fifties, sixties, seventies, eighties, nintiesPlus;
    private int dataTotal;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Chart");

        setContentView(R.layout.activity_chart);
        dataRepository = new DataRepository(this);

        pieChartView = findViewById(R.id.pieChart);
        barChartView = findViewById(R.id.barChart);

//get user's data for display. may want to create a dB class to only grab that info.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                data.addAll(dataRepository.getDataList());
                dataQueried = true;
            }
        });

        //quick and dirty fix. need to implement async class and use onPostExecute
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dataTotal = data.size();

//Generating slice data, simple categorisation of dB levels and taking percents.
        for (Data dataPoint : data) {
            Double dB = dataPoint.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                float addPercent = 100 / dataTotal;
                if (dB > 90) {
                    nintiesPlus += addPercent;
                } else if (dB > 80) {
                    eighties += addPercent;
                } else if (dB > 70) {
                    seventies += addPercent;
                } else if (dB > 60) {
                    sixties++;
                } else if (dB > 50) {
                    fifties += addPercent;
                } else if (dB > 40) {
                    fourties += addPercent;
                } else if (dB > 30) {
                    thirties += addPercent;
                }
            }
        }

        List pieData = new ArrayList<>();
        pieData.add(new SliceValue(0, Color.GREEN).setLabel("30-39 dB").setTarget(thirties));
        pieData.add(new SliceValue(0, Color.BLUE).setLabel("40-49 dB").setTarget(fourties));
        pieData.add(new SliceValue(0, Color.CYAN).setLabel("50-59 dB").setTarget(fifties));
        pieData.add(new SliceValue(0, Color.GRAY).setLabel("60-69 dB").setTarget(sixties));
        pieData.add(new SliceValue(0, Color.YELLOW).setLabel("70-79 dB").setTarget(seventies));
        pieData.add(new SliceValue(0, Color.MAGENTA).setLabel("80-89 dB").setTarget(eighties));
        pieData.add(new SliceValue(0, Color.RED).setLabel(">90 dB").setTarget(nintiesPlus));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterCircleScale(0.7f).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);

        pieChartView.setOnValueTouchListener(new ValueTouchListener(pieChartData, pieChartView));
        pieChartView.startDataAnimation();

        //users daily chart, TODO
        int numColumns = 7;

        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;

        for (int i = 0; i < numColumns; i++) {
            values = new ArrayList<>();
//            for(int j=0;j<7;j++){
            //placeholder subcolumn value for testing, need to split data by type and add colours and labels.
            values.add(new SubcolumnValue(thirties, Color.GREEN));
            values.add(new SubcolumnValue(fourties, Color.BLUE));
            values.add(new SubcolumnValue(fifties, Color.CYAN));
            values.add(new SubcolumnValue(sixties, Color.GRAY));
            values.add(new SubcolumnValue(seventies, Color.YELLOW));
            values.add(new SubcolumnValue(eighties, Color.MAGENTA));
            values.add(new SubcolumnValue(nintiesPlus, Color.RED));
//            }
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }

        ColumnChartData barChartData = new ColumnChartData(columns);
        barChartData.setStacked(true);

        barChartView.setColumnChartData(barChartData);
        barChartView.setHorizontalScrollBarEnabled(true);
        barChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

            }

            @Override
            public void onValueDeselected() {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onEnterAnimationComplete() {
        if (data.isEmpty()) {
            Toast.makeText(this, dataQueried.toString(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "#datapoints collected: " + data.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private class ValueTouchListener implements PieChartOnValueSelectListener {
        private PieChartView pieChartView;
        private PieChartData pieChartData;

        public ValueTouchListener(PieChartData pieChartData, PieChartView pieChartView) {
            this.pieChartData = pieChartData;
            this.pieChartView = pieChartView;
        }

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
            pieChartData.setHasCenterCircle(true).setCenterCircleScale(0.7f).setCenterText1("Your exposure to " + String.valueOf(value.getLabelAsChars()) + " was").setCenterText1FontSize(15).setCenterText1Color(Color.parseColor("#0097A7")).setCenterText2(String.valueOf(Math.round(value.getValue())) + "%").setCenterText2FontSize(10).setCenterText2Color(Color.parseColor("#0097A7"));
            pieChartView.setPieChartData(pieChartData);
            Toast.makeText(getApplication().getApplicationContext(), "db Range: " + String.valueOf(value.getLabelAsChars()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            pieChartData.setHasCenterCircle(true).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
            pieChartView.setPieChartData(pieChartData);
        }
    }

}
