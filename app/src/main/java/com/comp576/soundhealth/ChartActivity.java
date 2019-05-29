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

import androidx.lifecycle.Observer;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;

public class ChartActivity extends AppCompatActivity{
    PieChartView pieChartView;
    private ArrayAdapter<Data> adapter;
    private List<Data> data = new ArrayList<>();
    private ListView dataListView;
    private DataRepository dataRepository;
    private Boolean dataQueried=false;


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        dataRepository = new DataRepository(this);

        pieChartView = findViewById(R.id.chart);



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                data.addAll(dataRepository.getDataList());
                dataQueried=true;
            }
        });



//Initial dummy data, TODO generate real data from user DB.
        List pieData = new ArrayList<>();
        pieData.add(new SliceValue(50, Color.GREEN).setLabel("30-39 dB"));
        pieData.add(new SliceValue(22, Color.BLUE).setLabel("40-49 dB"));
        pieData.add(new SliceValue(11, Color.CYAN).setLabel("50-59 dB"));
        pieData.add(new SliceValue(6, Color.GRAY).setLabel("60-69 dB"));
        pieData.add(new SliceValue(5, Color.YELLOW).setLabel("70-79 dB"));
        pieData.add(new SliceValue(4, Color.MAGENTA).setLabel("80-89 dB"));
        pieData.add(new SliceValue(2, Color.RED).setLabel(">90 dB"));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);

        pieChartView.setOnValueTouchListener(new ValueTouchListener(pieChartData,pieChartView));


//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
//        //adapter setup
//        if (findViewById(R.id.dataListView) != null) {
//            dataListView = findViewById(R.id.dataListView);
//            dataListView.setAdapter(adapter);
////            dataListView.setOnItemClickListener(this);
//        }
//
//        dataRepository.getAllData().observe(this, new Observer<List<Data>>() {
//            @Override
//            public void onChanged(List<Data> updatedData) {
//// update the contacts list when the database changes
//                adapter.clear();
//                adapter.addAll(updatedData);
//            }
//        });

        if(data.isEmpty()){
            Toast.makeText(this,dataQueried.toString(),Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, data.get(0).toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private class ValueTouchListener implements PieChartOnValueSelectListener{
        private PieChartView pieChartView;
        private PieChartData pieChartData;

        public ValueTouchListener(PieChartData pieChartData, PieChartView pieChartView){
            this.pieChartData=pieChartData;
            this.pieChartView=pieChartView;
        }


        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
                pieChartData.setHasCenterCircle(true).setCenterText1("Your exposure to "+String.valueOf(value.getLabelAsChars())+" was").setCenterText1FontSize(15).setCenterText1Color(Color.parseColor("#0097A7"));
            pieChartData.setHasCenterCircle(true).setCenterText2(String.valueOf(value.getValue())).setCenterText2FontSize(10).setCenterText2Color(Color.parseColor("#0097A7"));

            pieChartView.setPieChartData(pieChartData);
            Toast.makeText(getApplication().getApplicationContext(),"value selected: "+value,Toast.LENGTH_LONG).show();

            }

        @Override
        public void onValueDeselected() {
                pieChartData.setHasCenterCircle(true).setCenterText1("Your Sound Profile").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
                pieChartView.setPieChartData(pieChartData);

            }
    }

}
