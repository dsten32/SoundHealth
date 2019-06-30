package com.comp576.soundhealth;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
    private List<Data> data = new ArrayList<>();
    private DataRepository dataRepository;
    private Boolean dataQueried = false;
    private float totalThirties, totalForties, totalFifties, totalSixties, totalSeventies, totalEighties, totalNintiesPlus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Chart");

        setContentView(R.layout.activity_chart);
        HorizontalScrollView hScrollView = (HorizontalScrollView) findViewById(R.id.barChartScroll);

        HashMap<String,Integer> chartColours = new HashMap<>();
        int thirties = Color.GREEN;
        chartColours.put("thirties", thirties);
        int forties = Color.BLUE;
        chartColours.put("forties", forties);
        int fifties = Color.CYAN;
        chartColours.put("fifties", fifties);
        int sixties = Color.GRAY;
        chartColours.put("sixties", sixties);
        int seventies = Color.YELLOW;
        chartColours.put("seventies", seventies);
        int eighties = Color.MAGENTA;
        chartColours.put("eighties", eighties);
        int ninties = Color.RED;
        chartColours.put("ninties", ninties);

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

        int dataTotal = data.size();

//Generating slice data, simple categorisation of dB levels and taking percents.
        for (Data dataPoint : data) {
            Double dB = dataPoint.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                float addPercent = 100 / dataTotal;
                if (dB > 90) totalNintiesPlus += addPercent;
                else if (dB > 80) totalEighties += addPercent;
                else if (dB > 70) totalSeventies += addPercent;
                else if (dB > 60) totalSixties += addPercent;
                else if (dB > 50) totalFifties += addPercent;
                else if (dB > 40) totalForties += addPercent;
                else if (dB > 30) totalThirties += addPercent;
            }
        }

        List pieData = new ArrayList<>();
        pieData.add(new SliceValue(0, chartColours.get("thirties")).setLabel("30-39 dB").setTarget(totalThirties));
        pieData.add(new SliceValue(0, chartColours.get("forties")).setLabel("40-49 dB").setTarget(totalForties));
        pieData.add(new SliceValue(0, chartColours.get("fifties")).setLabel("50-59 dB").setTarget(totalFifties));
        pieData.add(new SliceValue(0, chartColours.get("sixties")).setLabel("60-69 dB").setTarget(totalSixties));
        pieData.add(new SliceValue(0, chartColours.get("seventies")).setLabel("70-79 dB").setTarget(totalSeventies));
        pieData.add(new SliceValue(0, chartColours.get("eighties")).setLabel("80-89 dB").setTarget(totalEighties));
        pieData.add(new SliceValue(0, chartColours.get("ninties")).setLabel(">90 dB").setTarget(totalNintiesPlus));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterCircleScale(0.7f).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);

        pieChartView.setOnValueTouchListener(new ValueTouchListener(pieChartData, pieChartView));
        pieChartView.startDataAnimation();

        //users daily chart, TODO
        //ok, lest try creating a hashmap of date:datapoint pairs. need to convert the datapoint date string back into a date
        //after that add the hashmap to a TreeMap, should sort on date. then can loop through,
        // use the key as column label and datapoints as column values. how will that work?
        // could be instead of using adat points we do similar to the piechart.
        // for each map key there are 7 string: int pairs. if dB value in category then increment the int with that key.

        TreeMap<Date, TreeMap> dailyValues = new TreeMap<>();
        float dailyThirties, dailyForties, dailyFifties, dailySixties, dailySeventies, dailyEighties, dailyNintiesPlus;

        TreeMap<String, Float> dayValues = new TreeMap<>();

        for (Data data : data) {
            Date datapointDate = null;
            try {
                datapointDate = new SimpleDateFormat("dd-MMM-yyyy").parse(data.date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //idea here is to have each day with it's own set of total dB range values. not sure order of operations to get that working right.
            dailyValues.put(datapointDate, dayValues);
            //get number of datapoints in each category for the day.
            Double dB = data.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                if (dB > 90) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailyNintiesPlus = dayValues.containsKey("ninties") ? dayValues.get("ninties") : 0;
                        dayValues.put("ninties", dailyNintiesPlus+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailyNintiesPlus = 1;
                        dayValues.put("ninties", dailyNintiesPlus);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 80) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailyEighties = dayValues.containsKey("eighties") ? dayValues.get("eighties") : 0;
                        dayValues.put("eighties", dailyEighties+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailyEighties = 1;
                        dayValues.put("eighties", dailyEighties);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 70) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailySeventies = dayValues.containsKey("seventies") ? dayValues.get("seventies") : 0;
                        dayValues.put("seventies", dailySeventies+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailySeventies = 1;
                        dayValues.put("seventies", dailySeventies);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 60) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailySixties = dayValues.containsKey("sixties") ? dayValues.get("sixties") : 0;
                        dayValues.put("sixties", dailySixties+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailySixties = 1;
                        dayValues.put("sixties", dailySixties);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 50) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailyFifties = dayValues.containsKey("fifties") ? dayValues.get("fifties") : 0;
                        dayValues.put("fifties", dailyFifties+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailyFifties = 1;
                        dayValues.put("fifties", dailyFifties);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 40) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailyForties = dayValues.containsKey("forties") ? dayValues.get("forties") : 0;
                        dayValues.put("forties", dailyForties+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailyForties = 1;
                        dayValues.put("forties", dailyForties);
                        dailyValues.put(datapointDate, dayValues);
                    }
                } else if (dB > 30) {
                    if (dailyValues.containsKey(datapointDate)) {
                        dayValues = dailyValues.get(datapointDate);
                        dailyThirties = dayValues.containsKey("thirties") ? dayValues.get("thirties") : 0;
                        dayValues.put("thirties", dailyThirties+=1.0f);
                        dailyValues.put(datapointDate, dayValues);
                    } else {
                        dailyThirties = 1;
                        dayValues.put("thirties", dailyThirties);
                        dailyValues.put(datapointDate, dayValues);
                    }
                }
            }
        }


        //setup columns w/data
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;


        for (Date key : dailyValues.keySet()) {
            values = new ArrayList<>();
            dayValues = dailyValues.get(key);
            for(String dbKey:dayValues.keySet()){
                values.add(new SubcolumnValue(dayValues.get(dbKey),chartColours.get(dbKey)).setLabel(dbKey));
            }
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }

        ColumnChartData barChartData = new ColumnChartData(columns);
        barChartData.setStacked(true);

        List<AxisValue> xAxisValues = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");

        int i = 0;
        for(Date date:dailyValues.keySet()){
            AxisValue value = new AxisValue(i); //= dateFormat.format(date);
            value.setLabel(dateFormat.format(date));
            xAxisValues.add(value);
            i++;
        }

        Axis xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setName("Days");
        xAxis.setHasLines(true);
        xAxis.setHasSeparationLine(true);
        xAxis.setTextColor(Color.BLUE);

        Axis yAxis = new Axis();
        yAxis.setName("%");

        barChartData.setAxisXBottom(xAxis);
        barChartData.setAxisYRight(yAxis);
        barChartData.setFillRatio(0.7f);

        barChartView.setColumnChartData(barChartData);
        barChartView.setMinimumWidth(240*dailyValues.keySet().size());

        hScrollView.post(new Runnable() {
            public void run() {
                hScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        barChartView.setHorizontalScrollBarEnabled(true);
        barChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                Toast.makeText(getApplicationContext(),String.valueOf(value.getValue()),Toast.LENGTH_SHORT).show();
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
