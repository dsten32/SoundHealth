package com.comp576.soundhealth;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
    private Context context;
    private PieChartView pieChartView;
    private ColumnChartView barChartView;
    private List<Data> dataList = new ArrayList<>();
    private List<Data> dataDate = new ArrayList<>();
    //setting up a list of datapoint lists keyed by date
    private HashMap<Date,List<Data>> daysOfData = new HashMap<>();
    private List<Data> dailyDatapoints;
    private DataRepository dataRepository;
    private Boolean dataQueried = false;
    private Axis xAxis;
    private float totalThirties, totalForties, totalFifties, totalSixties, totalSeventies, totalEighties, totalNintiesPlus,longTouchx,longTouchy;
    public static String[] barInfoArray;
    public static int lowestDB = 0, highestDB=7;

    public void onCreate(Bundle savedInstanceState) {
        this.context=getApplicationContext();
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Chart");

        setContentView(R.layout.activity_chart);
        HorizontalScrollView hScrollView = (HorizontalScrollView) findViewById(R.id.barChartScroll);

        HashMap<String, Integer> chartColours = new HashMap<>();
        chartColours.put("thirties", Color.GREEN);
        chartColours.put("forties", Color.BLUE);
        chartColours.put("fifties", Color.CYAN);
        chartColours.put("sixties", Color.GRAY);
        chartColours.put("seventies", Color.YELLOW);
        chartColours.put("eighties", Color.MAGENTA);
        chartColours.put("ninties", Color.RED);

        dataRepository = new DataRepository(this);

        pieChartView = findViewById(R.id.pieChart);
        pieChartView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //start dialog stuff
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("pieDialog");
                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                fragmentTransaction.addToBackStack(null);

                DialogFragment dialogFragment = new PiechartFragment();
                dialogFragment.show(fragmentTransaction, "pieDialog");
                //end dialog stuff
//                Toast.makeText(context, "long touch", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        barChartView = findViewById(R.id.barChart);

//get user's dataList for display. may want to create a dB class to only grab that info.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                dataList.addAll(dataRepository.getDataList());
                dataQueried = true;
            }
        });

        //quick and dirty fix. need to implement async class and use onPostExecute
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //get total number of user datapoints so a percentage value
        // for each category can be displayed in the piechart
        float dataTotal = dataList.size();
        float addPercent;
        if (dataTotal != 0) {
            addPercent = 100 / dataTotal;
        } else {
            addPercent = 1.0f;
        }

        //Generating slice dataList, simple categorisation of dB levels and taking percents.
        for (Data dataPoint : dataList) {
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

        List<ArrayList> sliceParamsList = new ArrayList<>();

        sliceParamsList.add(new ArrayList(Arrays.asList("thirties","30-39 dB",totalThirties)));
        sliceParamsList.add(new ArrayList(Arrays.asList("forties","40-49 dB",totalForties)));
        sliceParamsList.add(new ArrayList(Arrays.asList("fifties","50-59 dB",totalFifties)));
        sliceParamsList.add(new ArrayList(Arrays.asList("sixties","60-69 dB",totalSixties)));
        sliceParamsList.add(new ArrayList(Arrays.asList("seventies","70-79 dB",totalSeventies)));
        sliceParamsList.add(new ArrayList(Arrays.asList("eighties","80-89 dB",totalEighties)));
        sliceParamsList.add(new ArrayList(Arrays.asList("ninties","<90 dB",totalNintiesPlus)));


        List pieData = new ArrayList<>();
        //need to work out how to exclude slices based on user db range choice.
        for(int slice=lowestDB;slice<highestDB;slice++){
            float dB = (float) sliceParamsList.get(slice).get(2);
            int colour = chartColours.get(sliceParamsList.get(slice).get(0).toString());
            String label = sliceParamsList.get(slice).get(1).toString();
            pieData.add(new SliceValue(dB,colour).setLabel(label));
        }

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterCircleScale(0.7f).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);

        pieChartView.setOnValueTouchListener(new ValueTouchListener(pieChartData, pieChartView));
        //this wa supposed to animate the chart such that the segments would grow to final size.
        //but it turns out animations aren't implemented for the piechart view. to remove this stuff
        // ie slice value as 0 and target values above.
        pieChartView.startDataAnimation();

        //users daily chart
        //ok, lets try creating a linkedhashmap of date:datapoint pairs. need to convert the datapoint date string back into a date
        //after that add the hashmap to a TreeMap, should sort on date. then can loop through,
        // use the key as column label and datapoints as column values. how will that work?
        // could be instead of using adat points we do similar to the piechart.
        // for each map key there are 7 string: int pairs. if dB value in category then increment the int with that key.

        TreeMap<Date, LinkedHashMap> dailyValues = new TreeMap<>();
        float dailyThirties, dailyForties, dailyFifties, dailySixties, dailySeventies, dailyEighties, dailyNintiesPlus;

        LinkedHashMap<String, Float> dayValues = new LinkedHashMap<>();
        int changeDateCount = 0;
        for (Data data : dataList) {
            Date datapointDate = null;
            try {
                datapointDate = new SimpleDateFormat("dd-MMM-yyyy").parse(data.date);
                //to ensure there is a bunch of days in the graph even if all the datapoints
                // were collected on one day, for demo purposes.
                long day = (long) 1000 * 60 * 60 * 24;
                if (changeDateCount % 14 == 0) {
                    datapointDate.setTime(datapointDate.getTime() - day);
                } else if (changeDateCount % 14 == 1) {
                    datapointDate.setTime(datapointDate.getTime() - 2 * day);
                } else if (changeDateCount % 14 == 2) {
                    datapointDate.setTime(datapointDate.getTime() - 3 * day);
                } else if (changeDateCount % 14 == 3) {
                    datapointDate.setTime(datapointDate.getTime() - 4 * day);
                } else if (changeDateCount % 14 == 4) {
                    datapointDate.setTime(datapointDate.getTime() - 5 * day);
                } else if (changeDateCount % 14 == 5) {
                    datapointDate.setTime(datapointDate.getTime() - 6 * day);
                } else if (changeDateCount % 14 == 6) {
                    datapointDate.setTime(datapointDate.getTime() - 7 * day);
                } else if (changeDateCount % 14 == 7) {
                    datapointDate.setTime(datapointDate.getTime() - 8 * day);
                } else if (changeDateCount % 14 == 8) {
                    datapointDate.setTime(datapointDate.getTime() - 9 * day);
                } else if (changeDateCount % 14 == 9) {
                    datapointDate.setTime(datapointDate.getTime() - 10 * day);
                } else if (changeDateCount % 14 == 10) {
                    datapointDate.setTime(datapointDate.getTime() - 11 * day);
                } else if (changeDateCount % 14 == 11) {
                    datapointDate.setTime(datapointDate.getTime() - 12 * day);
                } else if (changeDateCount % 14 == 12) {
                    datapointDate.setTime(datapointDate.getTime() - 13 * day);
                } else if (changeDateCount % 14 == 13) {
                    datapointDate.setTime(datapointDate.getTime() - 14 * day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            changeDateCount++;


            data.date = new SimpleDateFormat("dd-MMM-yyyy").format(datapointDate);
            dataDate.add(data);
            //adding datapoint to date keyed hashmap
            if(daysOfData.containsKey(datapointDate)){
                dailyDatapoints = daysOfData.get(datapointDate);
                dailyDatapoints.add(data);
                daysOfData.put(datapointDate,dailyDatapoints);
            } else {
                dailyDatapoints = new ArrayList<>();
                dailyDatapoints.add(data);
                daysOfData.put(datapointDate,dailyDatapoints);
            }

            /* idea here is to have each day with it's own set of total dB range values.
            not sure why I chose treemap here, to make best use
            */
            Double dB = data.dB;
            //check if null in case I forget to enable mic.
            if (dB != null) {
                LinkedHashMap<String, Float> tempDayValues = new LinkedHashMap<>();
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

        //setup columns w/dataList
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;

        for (Date key : dailyValues.keySet()) {
            values = new ArrayList<>();
            dayValues = dailyValues.get(key);
            float dayTotal = 0.0f;
            for (String dbKey : dayValues.keySet()) {
                dayTotal += dayValues.get(dbKey);
            }
            for (String dbKey : dayValues.keySet()) {
                values.add(new SubcolumnValue(dayValues.get(dbKey) / dayTotal, chartColours.get(dbKey)).setLabel(dbKey));
            }
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }

        ColumnChartData barChartData = new ColumnChartData(columns);
        barChartData.setStacked(true);

        List<AxisValue> xAxisValues = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

        int i = 0;
        for (Date date : dailyValues.keySet()) {
            AxisValue value = new AxisValue(i); //= dateFormat.format(date);
            value.setLabel(dateFormat.format(date));
            xAxisValues.add(value);
            i++;
        }

        xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setHasLines(true);
        xAxis.setHasSeparationLine(true);
        xAxis.setTextColor(Color.BLUE);

        Axis yAxis = new Axis();
        List<AxisValue> yAxisValues = new ArrayList<>();
        int percLabel = 0;
        for (float perc = 0.0f; perc < 1.1f; perc += 0.1f) {
            yAxisValues.add(new AxisValue(perc, ("" + percLabel).toCharArray()));
            percLabel += 10;
        }

        yAxis.setName("percent");
        yAxis.setValues(yAxisValues).setHasLines(true);
        yAxis.setTextColor(Color.DKGRAY);
        barChartData.setAxisXBottom(xAxis);
        barChartData.setAxisYRight(yAxis);
        barChartData.setAxisYLeft(yAxis);
        barChartData.setFillRatio(0.7f);


        barChartView.setColumnChartData(barChartData);
        //make sure the bars are set at a decent size relative to the number of days represented
        barChartView.setMinimumWidth(210 * dailyValues.keySet().size());

        //make sure the barchart is scrolled to the latest day inside the horizontal scrollview.
        hScrollView.post(new Runnable() {
            public void run() {
                hScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        barChartView.setHorizontalScrollBarEnabled(true);
        //unless I can think of a useful purpose for this value selection listener... dispose.
        barChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                //not sure if i should do anything with this
            }

            @Override
            public void onValueDeselected() {
            }
        });
        //attach listeners
        barChartView.setOnTouchListener(barChartTouchListener);
        barChartView.setOnLongClickListener(longClickListener);
    }
    //adding custom touch listener to get bar chart
    // column selected so the long click listener can access
    View.OnTouchListener barChartTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            longTouchx = event.getX();
            longTouchy = event.getY();
            v.performClick();
            return false;
        }
    };

//set up long click listener to get column touched index, filter the datalist to get the points for that column and show stats
    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            double highestDB=0;
            List<Integer> dayMinutes = new ArrayList<>();
            String columnDate;
            ((ColumnChartView)v).getChartRenderer().checkTouch(longTouchx,longTouchy);
            SelectedValue val = ((ColumnChartView)v).getChartRenderer().getSelectedValue();
            columnDate = String.valueOf(xAxis.getValues().get(Integer.parseInt(String.valueOf(val.getFirstIndex()))).getLabelAsChars());
            try {
            dailyDatapoints = daysOfData.get(new SimpleDateFormat("dd-MMM-yy").parse(columnDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            for (Data data : dailyDatapoints) {
                if(data.dB>highestDB){
                    highestDB = data.dB;
                }
                //get the datapoint time and turn into minutes, add to a list so we can get
                //the first datapoint time, last datapoint time and the average
                //number of mind between datapoints
                String[] sarr = data.time.split(":");
                dayMinutes.add(Integer.parseInt(sarr[0])*60 + Integer.parseInt(sarr[1]));
            }

            highestDB = (double)Math.round(highestDB*100)/100;
            Collections.sort(dayMinutes);
            int summedDiffs = 0;
            for(int i=dayMinutes.size()-1;i>0;i--){
                summedDiffs += dayMinutes.get(i) - dayMinutes.get(i-1);
            }
            int averageMins = summedDiffs/dayMinutes.size();
            String firstTime,lastTime;
            firstTime = String.valueOf(dayMinutes.get(0) / 60) +":"+ String.format("%1$" + 2 + "s", dayMinutes.get(0)%60).replace(' ', '0');
            lastTime = String.valueOf(dayMinutes.get(dayMinutes.size()-1) / 60) +":"+ String.format("%1$" + 2 + "s", dayMinutes.get(dayMinutes.size()-1)%60).replace(' ', '0');

            //start dialog stuff
            barInfoArray = new String[]{String.valueOf(dailyDatapoints.size()),firstTime,lastTime,String.valueOf(averageMins),String.valueOf(highestDB)};
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("barDialog");
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            DialogFragment dialogFragment = new BarInfoFragment();
            dialogFragment.show(fragmentTransaction, "barDialog");
            //end dialog stuff
            if(((ColumnChartView)v).getChartRenderer().isTouched()){
//                Toast.makeText(context,String.valueOf(dayCount),Toast.LENGTH_LONG).show();
//                Toast.makeText(context, String.valueOf(xAxis.getValues().get(Integer.parseInt(String.valueOf(val.getFirstIndex()))).getLabelAsChars()),Toast.LENGTH_LONG).show();
            }
            return false;
        }
    };

    //setting navigation bar to return to main activity
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onEnterAnimationComplete() {
        if (dataList.isEmpty()) {
            Toast.makeText(this, dataQueried.toString(), Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "#datapoints collected: " + dataList.size(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "db Range: " + String.valueOf(value.getLabelAsChars()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            pieChartData.setHasCenterCircle(true).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
            pieChartView.setPieChartData(pieChartData);
        }
    }

}
