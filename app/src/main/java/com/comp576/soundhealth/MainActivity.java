package com.comp576.soundhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    TextView location,dateTime;
    Button goToMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location = (TextView) findViewById(R.id.location);
        location.setText("new text I put here 'cos I could");

        dateTime = findViewById(R.id.dateTime);
        String date =new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date());
//        Date date = Calendar.getInstance().getTime();
        dateTime.setText(date);
        goToMap = (Button) findViewById(R.id.goToMap);


    }

    public void goToChart(View view){
        Intent goToChart = new Intent(this,ChartActivity.class);
        startActivity(goToChart);
    }

    public void goToMap(View view){
        Intent goToMap = new Intent(this,MapsActivity.class);
        startActivity(goToMap);
    }


}
