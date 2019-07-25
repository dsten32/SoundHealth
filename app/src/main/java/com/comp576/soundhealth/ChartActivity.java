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

        HashMap<String, Integer> chartColours = new HashMap<>();
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        int dataTotal = data.size();
        float addPercent;
        if(dataTotal!=0) {
            addPercent = 100 / dataTotal;
        } else {
            addPercent = 1;
        }

//Generating slice data, simple categorisation of dB levels and taking percents.
        for (Data dataPoint : data) {
            Double dB = dataPoint.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                if (dB > 90) totalNintiesPlus += addPercent;
                else if (dB > 80) totalEighties += addPercent;
                else if (dB > 70) totalSeventies += addPercent;
                else if (dB > 60) totalSixties += addPercent;
                else if (dB > 50) totalFifties += addPercent;
                else if (dB > 40) totalForties += addPercent;
                else if (dB < 40) totalThirties += addPercent;
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
        dailyThirties = dailyForties = dailyFifties = dailySixties = dailySeventies = dailyEighties = dailyNintiesPlus = 0.0f;

        TreeMap<String, Float> dayValues = new TreeMap<>();
        int changeDateount = 0;
        for (Data data : data) {
            Date datapointDate = null;
            try {
                datapointDate = new SimpleDateFormat("dd-MMM-yyyy").parse(data.date);
                //for making a bunch of days in the graph
                long day = (long) 1000 * 60 * 60 * 24;
                if (changeDateount % 14 == 0) {
                    datapointDate.setTime(datapointDate.getTime() - day);
                } else if (changeDateount % 14 == 1) {
                    datapointDate.setTime(datapointDate.getTime() - 2 * day);
                } else if (changeDateount % 14 == 2) {
                    datapointDate.setTime(datapointDate.getTime() - 3 * day);
                } else if (changeDateount % 14 == 3) {
                    datapointDate.setTime(datapointDate.getTime() - 4 * day);
                } else if (changeDateount % 14 == 4) {
                    datapointDate.setTime(datapointDate.getTime() - 5 * day);
                } else if (changeDateount % 14 == 5) {
                    datapointDate.setTime(datapointDate.getTime() - 6 * day);
                } else if (changeDateount % 14 == 6) {
                    datapointDate.setTime(datapointDate.getTime() - 7 * day);
                }else if (changeDateount % 14 == 7) {
                    datapointDate.setTime(datapointDate.getTime() - 8 * day);
                }else if (changeDateount % 14 == 8) {
                    datapointDate.setTime(datapointDate.getTime() - 9 * day);
                }else if (changeDateount % 14 == 9) {
                    datapointDate.setTime(datapointDate.getTime() - 10 * day);
                }else if (changeDateount % 14 == 10) {
                    datapointDate.setTime(datapointDate.getTime() - 11 * day);
                }else if (changeDateount % 14 == 11) {
                    datapointDate.setTime(datapointDate.getTime() - 12 * day);
                }else if (changeDateount % 14 == 12) {
                    datapointDate.setTime(datapointDate.getTime() - 13 * day);
                }else if (changeDateount % 14 == 13) {
                    datapointDate.setTime(datapointDate.getTime() - 14 * day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            changeDateount++;
            //idea here is to have each day with it's own set of total dB range values. not sure order of operations to get that working right.
//            dailyValues.put(datapointDate, dayValues);
            //get number of datapoints in each category for the day.
            Double dB = data.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                TreeMap<String, Float> tempDayValues = new TreeMap<>();
                dailyThirties = dailyForties = dailyFifties = dailySixties = dailySeventies = dailyEighties = dailyNintiesPlus = 0.0f;

                if (dB > 90) dailyNintiesPlus = 1f;
                else if (dB > 80) dailyEighties = 1f;
                else if (dB > 70) dailySeventies = 1f;
                else if (dB > 60) dailySixties = 1f;
                else if (dB > 50) dailyFifties = 1f;
                else if (dB > 40) dailyForties = 1f;
                else if (dB < 40) dailyThirties = 1f;

                if (dailyValues.containsKey(datapointDate)) {
                    tempDayValues = dailyValues.get(datapointDate);
                    dailyNintiesPlus += tempDayValues.containsKey("ninties") ? tempDayValues.get("ninties") : 0.0f;
                    tempDayValues.put("ninties", dailyNintiesPlus);
                    dailyEighties += tempDayValues.containsKey("eighties") ? tempDayValues.get("eighties") : 0.0f;
                    tempDayValues.put("eighties", dailyEighties);
                    dailySeventies += tempDayValues.containsKey("seventies") ? tempDayValues.get("seventies") : 0.0f;
                    tempDayValues.put("seventies", dailySeventies);
                    dailySixties += tempDayValues.containsKey("sixties") ? tempDayValues.get("sixties") : 0.0f;
                    tempDayValues.put("sixties", dailySixties);
                    dailyFifties += tempDayValues.containsKey("fifties") ? tempDayValues.get("fifties") : 0.0f;
                    tempDayValues.put("fifties", dailyFifties);
                    dailyForties += tempDayValues.containsKey("forties") ? tempDayValues.get("forties") : 0.0f;
                    tempDayValues.put("forties", dailyForties);
                    dailyThirties += tempDayValues.containsKey("thirties") ? tempDayValues.get("thirties") : 0.0f;
                    tempDayValues.put("thirties", dailyThirties);
                    dailyValues.put(datapointDate, tempDayValues);
                } else {
                    tempDayValues.put("ninties", dailyNintiesPlus);
                    tempDayValues.put("eighties", dailyEighties);
                    tempDayValues.put("seventies", dailySeventies);
                    tempDayValues.put("sixties", dailySixties);
                    tempDayValues.put("fifties", dailyFifties);
                    tempDayValues.put("forties", dailyForties);
                    tempDayValues.put("thirties", dailyThirties);
                    dailyValues.put(datapointDate, tempDayValues);
                }

            }
        }


        //setup columns w/data
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;


        for (Date key : dailyValues.keySet()) {
            values = new ArrayList<>();
            dayValues = dailyValues.get(key);
            float dayTotal=0.0f;
            for (String dbKey : dayValues.keySet()) {
                dayTotal+=dayValues.get(dbKey);
            }
            for (String dbKey : dayValues.keySet()) {
                values.add(new SubcolumnValue(dayValues.get(dbKey)/dayTotal, chartColours.get(dbKey)).setLabel(dbKey));
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
        for (Date date : dailyValues.keySet()) {
            AxisValue value = new AxisValue(i); //= dateFormat.format(date);
            value.setLabel(dateFormat.format(date));
            xAxisValues.add(value);
            i++;
        }

        Axis xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setHasLines(true);
        xAxis.setHasSeparationLine(true);
        xAxis.setTextColor(Color.BLUE);

        Axis yAxis = new Axis();
        List<AxisValue> yAxisValues = new ArrayList<>();
        int percLabel=0;
        for(float perc=0.0f;perc<1.1f;perc+=0.1f){
            yAxisValues.add(new AxisValue(perc,("" + percLabel).toCharArray()));
            percLabel+=10;
        }

        yAxis.setName("percent");
        yAxis.setValues(yAxisValues).setHasLines(true);
        yAxis.setTextColor(Color.DKGRAY);
        barChartData.setAxisXBottom(xAxis);
        barChartData.setAxisYRight(yAxis);
        barChartData.setAxisYLeft(yAxis);
        barChartData.setFillRatio(0.7f);

        barChartView.setColumnChartData(barChartData);
        barChartView.setMinimumWidth(210 * dailyValues.keySet().size());

        hScrollView.post(new Runnable() {
            public void run() {
                hScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        barChartView.setHorizontalScrollBarEnabled(true);
        barChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                Toast.makeText(getApplicationContext(), String.valueOf(Math.round(value.getValue()*100)+"%"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                Toast.makeText(getApplicationContext(), "deselected", Toast.LENGTH_LONG).show();
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
