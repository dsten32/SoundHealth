package com.comp576.soundhealth;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Range;
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
import java.util.Objects;
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
    private HashMap<Date, TreeMap<LocalTime, Data>> dataListByDate = new HashMap<>();
    private TreeMap<LocalTime, Data> dailyDatapoints = new TreeMap<>();
    //private List<Data> dailyDatapoints;
    private HashMap<Range<Integer>, String[]> categoryList;
    private DataRepository dataRepository;
    private Boolean dataQueried = false;
    private Axis xAxis;
    private TreeMap<Date, LinkedHashMap<String, Float>> dailyValues;
    //this is a linkedhashmap rather than a plain hashmap so that columns will
    //always have the same order. probably a better way to do this
    private LinkedHashMap<String, Float> dayValues;
    private float longTouchx, longTouchy;
    public static String[] barInfoArray;
    public int lowestDB = 0, highestDB = 7;
    public boolean isRelative, isAbsolute = true, isTimeline, isColourBlind;

    public void onCreate(Bundle savedInstanceState) {
        this.context = getApplicationContext();
        super.onCreate(savedInstanceState);
//        isRelative=true;
        dataRepository = new DataRepository(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Chart");

        setContentView(R.layout.activity_chart);

        setChartColours();
        setChartOptions();
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
                return false;
            }
        });

        barChartView = findViewById(R.id.barChart);
        barChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                //should add code that shows info about the subcolumn selected.
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

    private void setChartOptions(){
        //set up map that can get labels for db ranges
        categoryList = new HashMap<Range<Integer>, String[]>();
        categoryList.put(Range.create(0, 39), new String[]{"thirties", "<30-39dB"});
        categoryList.put(Range.create(40, 49), new String[]{"forties", "40-49dB"});
        categoryList.put(Range.create(50, 59), new String[]{"fifties", "50-59dB"});
        categoryList.put(Range.create(60, 69), new String[]{"sixties", "60-69dB"});
        categoryList.put(Range.create(70, 79), new String[]{"seventies", "70-79dB"});
        categoryList.put(Range.create(80, 89), new String[]{"eighties", "80-89dB"});
        categoryList.put(Range.create(90, 99), new String[]{"nineties", "90-9dB"});
    }

    public void setChartColours(){
        //set up chart colours
        chartColours = new HashMap<>();
        if(isColourBlind) {
            chartColours.put("thirties", Color.parseColor("#DCDCDC"));
            chartColours.put("forties", Color.parseColor("#999999"));
            chartColours.put("fifties", Color.parseColor("#777777"));
            chartColours.put("sixties", Color.parseColor("#555555"));
            chartColours.put("seventies", Color.parseColor("#333333"));
            chartColours.put("eighties", Color.parseColor("#111111"));
            chartColours.put("ninties", Color.parseColor("#000000"));
        } else {
            chartColours.put("thirties", Color.GREEN);
            chartColours.put("forties", Color.BLUE);
            chartColours.put("fifties", Color.CYAN);
            chartColours.put("sixties", Color.GRAY);
            chartColours.put("seventies", Color.YELLOW);
            chartColours.put("eighties", Color.MAGENTA);
            chartColours.put("ninties", Color.RED);
        }
    }

    private class UserDataAsyncTask extends AsyncTask<Void, Void, Void> {

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
            Toast.makeText(context, String.valueOf(dataList.size()), Toast.LENGTH_LONG).show();
            pieChartAddData();
            barChartAddData();
        }
    }

    //method to setup the piechart and manage re-building on user changing dB range for display
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pieChartAddData() {
        //get total number of user datapoints so a percentage value
        // for each category can be displayed in the piechart
        float addPercent = 100 /(( dataList.size() != 0 ) ? (float)dataList.size() : 1.0f);

        //Generating piechart slice dataList, simple categorisation of dB levels and taking percents.
        List<ArrayList> sliceParamsList = new ArrayList<>();
        //set up list with category dets arrays
        for (Range<Integer> range : categoryList.keySet()) {
            sliceParamsList.add(new ArrayList(Arrays.asList(categoryList.get(range)[0], categoryList.get(range)[1], 0.0f)));
        }
        for (Data dataPoint : dataList) {
            int dB = (int) Math.round(dataPoint.dB);
            if(dB != 0) {
                for (Range<Integer> range : categoryList.keySet()) {
                    if (range.contains(dB)) {
                        for (int subList = 0;subList < sliceParamsList.size(); subList++){
                            if(sliceParamsList.get(subList).contains(categoryList.get(range)[0])){
                                ArrayList sub = sliceParamsList.get(subList);
                                float tempFlt = (float)sub.get(2) + addPercent;
                                sliceParamsList.remove(sub);
                                sliceParamsList.add(new ArrayList(Arrays.asList(categoryList.get(range)[0], categoryList.get(range)[1], tempFlt )));
                                break;
                            }
                        }
                    }
                }
            }
        }
        //new range choice code
        String[] defaultPieDBs = new String[]{"thirties","forties","fifties","sixties","seventies","eighties","ninties"};
        String[] userPieDBs = Arrays.copyOfRange(defaultPieDBs,lowestDB,highestDB-1);

        List pieData = new ArrayList<>();
        for (ArrayList subList : sliceParamsList) {
            if(Arrays.stream(userPieDBs).anyMatch(subList.get(0)::equals)  ){
                float dB = (float) subList.get(2);
                int colour = chartColours.get(subList.get(0).toString());
                String label = subList.get(1).toString();
                pieData.add(new SliceValue(dB, colour).setLabel(label));
            }
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
        //creating a linkedhashmap of date:datapoint pairs. need to convert the datapoint date string back into a date
        //after that add the hashmap to a TreeMap, should sort on date. then can loop through,
        // use the key as column label and datapoints as column values.
        // could be instead of using data points we do similar to the piechart.
        // for each map key there are 7 string: int pairs. if dB value in category then increment the int with that key.
        HorizontalScrollView hScrollView = (HorizontalScrollView) findViewById(R.id.barChartScroll);

        dailyValues = new TreeMap<>();
        dayValues = new LinkedHashMap<>();
//        int changeDateCount = 0;
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
            if (dataListByDate.containsKey(datapointDate)) {
                //retrieve treemap
                dailyDatapoints = dataListByDate.get(datapointDate);
                //add datapoin with time key
                dailyDatapoints.put(LocalTime.parse(data.time), data);
                //put time keyed map back in date keyed map
                dataListByDate.put(datapointDate, dailyDatapoints);
            } else {
                dailyDatapoints = new TreeMap<>();
                dailyDatapoints.put(LocalTime.parse(data.time), data);
                dataListByDate.put(datapointDate, dailyDatapoints);
            }

            /* idea here is to have each day with it's own set of total dB range values.
             */
            //check if null in case I forget to enable mic.
            if (data.dB != null) {
                int dB = (int) Math.round(data.dB);
                LinkedHashMap<String, Float> tempDayValues = new LinkedHashMap<>();

                if (dailyValues.containsKey(datapointDate)) {
                    tempDayValues = dailyValues.get(datapointDate);
                    for (Range<Integer> range : categoryList.keySet()) {
                        if (range.contains(dB)) {
                            if (tempDayValues.containsKey(categoryList.get(range)[0])) {
                                tempDayValues.put(categoryList.get(range)[0], tempDayValues.get(categoryList.get(range)[0]) + 1f);
                                dailyValues.put(datapointDate, tempDayValues);
                            } else {
                                tempDayValues.put(categoryList.get(range)[0], 1f);
                                dailyValues.put(datapointDate, tempDayValues);
                            }
                        }
                    }
                } else {
                    for (Range<Integer> range : categoryList.keySet()) {
                        if (range.contains(dB)) {
                            tempDayValues.put(categoryList.get(range)[0], 1f);
                            dailyValues.put(datapointDate, tempDayValues);
                        }
                    }
                }
            }
        }
        //setup columns w/dataList
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        List<AxisValue> xAxisValues = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

        //set up axis ans columns for barchart depending on type chosen
        int xAxisIndex = 0;
        for (Date date : dailyValues.keySet()) {
            values = new ArrayList<>();
            AxisValue value = new AxisValue(xAxisIndex); //= dateFormat.format(date);
            value.setLabel(dateFormat.format(date));
            xAxisValues.add(value);
            xAxisIndex++;
            if (isAbsolute || isRelative) {
                dayValues = dailyValues.get(date);
                float dayTotal = 0.0f;
                for (String dBRange : dayValues.keySet()) {
                    if (dayValues.get(dBRange) > 0) {
                        dayTotal += dayValues.get(dBRange);
                    }
                }
                for (String dBRange : dayValues.keySet()) {
                    if (isRelative && dayValues.get(dBRange) > 0) {
                        values.add(new SubcolumnValue(dayValues.get(dBRange) / dayTotal, chartColours.get(dBRange)).setLabel(dBRange));
                    } else if (isAbsolute) {
                        if (dayValues.get(dBRange) != 0) {
                            values.add(new SubcolumnValue(dayValues.get(dBRange), chartColours.get(dBRange)).setLabel(dBRange));
                        }
                    }
                }

            } else if (isTimeline) {
                for (LocalTime time : dataListByDate.get(date).keySet()) {
                    for (Range<Integer> range : categoryList.keySet()) {
                        range.contains((int) 9.0);
                        if (range.contains((int) Math.round(dataListByDate.get(date).get(time).dB))) {
                            values.add(new SubcolumnValue(1, chartColours.get(categoryList.get(range)[0])).setLabel(categoryList.get(range)[1]));
                        }
                    }
                }
            }
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }

        //generate yaxis
        Axis yAxis = new Axis();
        List<AxisValue> yAxisValues = new ArrayList<>();
        if (isRelative) {
            yAxis.setName("percent");
            int percLabel = 0;
            for (float perc = 0.0f; perc < 1.1f; perc += 0.1f) {
                yAxisValues.add(new AxisValue(perc, ("" + percLabel).toCharArray()));
                percLabel += 10;
            }
        } else if (isAbsolute || isTimeline) {
            yAxis.setName("number of points");
            int maxDayPoints = 0;
            for (Date date : dataListByDate.keySet()) {
                TreeMap<LocalTime, Data> timeMap = dataListByDate.get(date);
                if (timeMap.size() > maxDayPoints) {
                    maxDayPoints = timeMap.size();
                }
            }
            int absLabel = 0;
            for (int abs = 0; abs < maxDayPoints + 4; abs += 1) {
                yAxisValues.add(new AxisValue(abs, ("" + absLabel).toCharArray()));
                absLabel += 1;
            }
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
        barChartView.setMinimumWidth(210 * (dailyValues.keySet().size() + 1));

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
            double highestDB = 0;
            double lowDB = 100;
            List<Integer> dayMinutes = new ArrayList<>();
            String columnDate;
            ((ColumnChartView) v).getChartRenderer().checkTouch(longTouchx, longTouchy);
            SelectedValue val = ((ColumnChartView) v).getChartRenderer().getSelectedValue();
            if (val.getFirstIndex() >= 0) {
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
                    if (dailyDatapoints.get(time).dB < lowDB) {
                        lowDB = dailyDatapoints.get(time).dB;
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
