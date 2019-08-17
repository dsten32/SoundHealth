package com.comp576.soundhealth;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import java.time.LocalTime;

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
    HashMap<String, Integer> chartColours;
    private PieChartView pieChartView;
    private ColumnChartView barChartView;
    private List<Data> dataList = new ArrayList<>();
    //setting up a list of datapoint lists keyed by date and time,
//    private HashMap<Date,List<Data>> dataListByDate = new HashMap<>();
    private HashMap<Date,TreeMap<LocalTime,Data>> dataListByDate = new HashMap<>();
    private TreeMap<LocalTime,Data> dailyDatapoints = new TreeMap<>();
    //private List<Data> dailyDatapoints;
    private DataRepository dataRepository;
    private Boolean dataQueried = false;
    private Axis xAxis;
    private TreeMap<Date, LinkedHashMap> dailyValues;
    private LinkedHashMap<String, Float> dayValues;
    private float totalThirties, totalForties, totalFifties, totalSixties, totalSeventies, totalEighties, totalNintiesPlus,longTouchx,longTouchy;
    public static String[] barInfoArray;
    public int lowestDB = 0, highestDB=7;
    public boolean isRelative, isAbsolute=true, isTimeline;

    public void onCreate(Bundle savedInstanceState) {
        this.context=getApplicationContext();
        super.onCreate(savedInstanceState);
//        isRelative=true;
        dataRepository = new DataRepository(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Chart");

        setContentView(R.layout.activity_chart);

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

        new UserDataAsyncTask().execute();

    }

    private class UserDataAsyncTask extends AsyncTask<Void,Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            dataList.addAll(dataRepository.getDataList());
            dataQueried = true;
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(context,String.valueOf(dataList.size()),Toast.LENGTH_LONG).show();
            pieChartAddData();
            barChartAddData();
        }
    }


    //method to setup the piechart and manage re-building on user changing dB range for display
    public void pieChartAddData() {
        //set up chart colours
        chartColours = new HashMap<>();
        chartColours.put("thirties", Color.GREEN);
        chartColours.put("forties", Color.BLUE);
        chartColours.put("fifties", Color.CYAN);
        chartColours.put("sixties", Color.GRAY);
        chartColours.put("seventies", Color.YELLOW);
        chartColours.put("eighties", Color.MAGENTA);
        chartColours.put("ninties", Color.RED);

        //get total number of user datapoints so a percentage value
        // for each category can be displayed in the piechart
        float dataTotal = dataList.size();
        float addPercent;
        if (dataTotal != 0) {
            addPercent = 100 / dataTotal;
        } else {
            addPercent = 1.0f;
        }

        //Generating piechart slice dataList, simple categorisation of dB levels and taking percents.
        //first reset the percent variables so they are correct when piechart updates
        totalThirties=totalForties=totalFifties=totalSixties=totalSeventies=totalEighties=totalNintiesPlus=0.0f;
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

    }

    //method for setting up barchart.
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void barChartAddData() {
        //users daily chart
        //ok, lets try creating a linkedhashmap of date:datapoint pairs. need to convert the datapoint date string back into a date
        //after that add the hashmap to a TreeMap, should sort on date. then can loop through,
        // use the key as column label and datapoints as column values. how will that work?
        // could be instead of using data points we do similar to the piechart.
        // for each map key there are 7 string: int pairs. if dB value in category then increment the int with that key.
        HorizontalScrollView hScrollView = (HorizontalScrollView) findViewById(R.id.barChartScroll);

        dailyValues = new TreeMap<>();
        float dailyThirties, dailyForties, dailyFifties, dailySixties, dailySeventies, dailyEighties, dailyNintiesPlus;

        dayValues = new LinkedHashMap<>();
        int changeDateCount = 0;
        for (Data data : dataList) {
            Date datapointDate = null;
            try {
                datapointDate = new SimpleDateFormat("dd-MMM-yyyy").parse(data.date);
                //to ensure there is a bunch of days in the graph even if all the datapoints
                //were collected on one day, for demo purposes.
//                long day = (long) 1000 * 60 * 60 * 24;
//                datapointDate.setTime(datapointDate.getTime() - (changeDateCount % 14)*day);
//                data.date = new SimpleDateFormat("dd-MMM-yyyy").format(datapointDate);
//                changeDateCount++;
            } catch (ParseException e) {
                e.printStackTrace();
            }


            //adding datapoint to date keyed hashmap of time keyed treemap
            if(dataListByDate.containsKey(datapointDate)){
                //retrieve treemap
                dailyDatapoints = dataListByDate.get(datapointDate);
                //add datapoin with time key
                dailyDatapoints.put(LocalTime.parse(data.time +":00"),data);
                //put time keyed map back in date keyed map
                dataListByDate.put(datapointDate,dailyDatapoints);
            } else {
                dailyDatapoints = new TreeMap<>();
                dailyDatapoints.put(LocalTime.parse(data.time+":00"),data);
                dataListByDate.put(datapointDate,dailyDatapoints);
            }

            /* idea here is to have each day with it's own set of total dB range values.
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
        //todo set up a column that has data by time via the time keyed treemap
        //setup columns w/dataList
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        List<AxisValue> xAxisValues = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

//        for (Date date : dailyValues.keySet()) {
//
//        }
        int xAxisIndex = 0;
        for (Date key : dailyValues.keySet()) {
            values = new ArrayList<>();
            AxisValue value = new AxisValue(xAxisIndex); //= dateFormat.format(date);
            value.setLabel(dateFormat.format(key));
            xAxisValues.add(value);
            xAxisIndex++;
            dayValues = dailyValues.get(key);
            float dayTotal = 0.0f;
            for (String dbKey : dayValues.keySet()) {
                dayTotal += dayValues.get(dbKey);
            }
            for (String dbKey : dayValues.keySet()) {
                if(isRelative) {
                    values.add(new SubcolumnValue(dayValues.get(dbKey) / dayTotal, chartColours.get(dbKey)).setLabel(dbKey));
                } else if (isAbsolute){
                    values.add(new SubcolumnValue(dayValues.get(dbKey), chartColours.get(dbKey)).setLabel(dbKey));
                } else if (isTimeline){
                    //todo
                }
            }
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }

        //generate yaxis
        Axis yAxis = new Axis();
        List<AxisValue> yAxisValues = new ArrayList<>();
        if(isRelative){
            yAxis.setName("percent");
            int percLabel = 0;
            for (float perc = 0.0f; perc < 1.1f; perc += 0.1f) {
                yAxisValues.add(new AxisValue(perc, ("" + percLabel).toCharArray()));
                percLabel += 10;
            }
        } else if (isAbsolute){
            yAxis.setName("number of points");
            //todo, figure out how to get the scale on this axis
            int maxDayPoints =0;
            for(Date date : dataListByDate.keySet()){
                TreeMap<LocalTime,Data> timeMap = dataListByDate.get(date);
                if(timeMap.size()>maxDayPoints){
                    maxDayPoints=timeMap.size();
                }
            }
            int absLabel = 0;
            for (int abs = 0; abs < maxDayPoints+5; abs += 1) {
                yAxisValues.add(new AxisValue(abs, ("" + absLabel).toCharArray()));
                absLabel += 1;
            }
        } else if (isTimeline){
            //todo
        }

        ColumnChartData barChartData = new ColumnChartData(columns);
        barChartData.setStacked(true);



        xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setHasLines(true);
        xAxis.setHasSeparationLine(true);
        xAxis.setTextColor(Color.BLUE);

        yAxis.setValues(yAxisValues).setHasLines(true);
        yAxis.setTextColor(Color.DKGRAY);

        barChartData.setAxisXBottom(xAxis);
        barChartData.setAxisYRight(yAxis);
        barChartData.setAxisYLeft(yAxis);
        barChartData.setFillRatio(0.7f);

        barChartView.setColumnChartData(barChartData);
        //make sure the bars are set at a decent size relative to the number of days represented
        barChartView.setMinimumWidth(210 * (dailyValues.keySet().size()+1));

        //make sure the barchart is scrolled to the latest day inside the horizontal scrollview.
        hScrollView.post(new Runnable() {
            public void run() {
                hScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        barChartView.setHorizontalScrollBarEnabled(true);
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
            if(val.getFirstIndex() >= 0) {
                columnDate = String.valueOf(xAxis.getValues().get(Integer.parseInt(String.valueOf(val.getFirstIndex()))).getLabelAsChars());
                try {
                    dailyDatapoints = dataListByDate.get(new SimpleDateFormat("dd-MMM-yy").parse(columnDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                for (LocalTime time : dailyDatapoints.keySet()) {
                    if (dailyDatapoints.get(time).dB > highestDB) {
                        highestDB = dailyDatapoints.get(time).dB;
                    }
                    //get the datapoint time and turn into minutes, add to a list so we can get
                    //the first datapoint time, last datapoint time and the average
                    //number of mind between datapoints
                    String[] sarr = dailyDatapoints.get(time).time.split(":");
                    dayMinutes.add(Integer.parseInt(sarr[0]) * 60 + Integer.parseInt(sarr[1]));
                }

                highestDB = (double) Math.round(highestDB * 100) / 100;
                Collections.sort(dayMinutes);
                int summedDiffs = 0;
                for (int i = dayMinutes.size() - 1; i > 0; i--) {
                    summedDiffs += dayMinutes.get(i) - dayMinutes.get(i - 1);
                }
                int averageMins = summedDiffs / dayMinutes.size();
                String firstTime, lastTime;
                firstTime = String.valueOf(dayMinutes.get(0) / 60) + ":" + String.format("%1$" + 2 + "s", dayMinutes.get(0) % 60).replace(' ', '0');
                lastTime = String.valueOf(dayMinutes.get(dayMinutes.size() - 1) / 60) + ":" + String.format("%1$" + 2 + "s", dayMinutes.get(dayMinutes.size() - 1) % 60).replace(' ', '0');

                //start dialog stuff
                barInfoArray = new String[]{String.valueOf(dailyDatapoints.size()), firstTime, lastTime, String.valueOf(averageMins), String.valueOf(highestDB)};
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("barDialog");
                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                fragmentTransaction.addToBackStack(null);

                DialogFragment dialogFragment = new BarInfoFragment();
                dialogFragment.show(fragmentTransaction, "barDialog");
                //end dialog stuff
                if (((ColumnChartView) v).getChartRenderer().isTouched()) {
//                Toast.makeText(context,String.valueOf(dayCount),Toast.LENGTH_LONG).show();
//                Toast.makeText(context, String.valueOf(xAxis.getValues().get(Integer.parseInt(String.valueOf(val.getFirstIndex()))).getLabelAsChars()),Toast.LENGTH_LONG).show();
                }
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

    public void showDialog(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("chartDialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment dialogFragment = new ChartSettingsFragment();
        dialogFragment.show(fragmentTransaction, "chartDialog");
    }


}
