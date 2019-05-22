package com.comp576.soundhealth;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;

public class ChartActivity extends AppCompatActivity{
    PieChartView pieChartView;


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        pieChartView = findViewById(R.id.chart);

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
