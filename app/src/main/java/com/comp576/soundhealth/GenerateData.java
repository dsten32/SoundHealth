package com.comp576.soundhealth;

import android.os.Build;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.RequiresApi;

//for generating lots of fake data in and around hamilton if I need it.
public class GenerateData {
    String date, time, userId;
    double lat, lng, dB;
    List<Data> dataList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Data> getFakeData() {
        String month;
        for (int i = 0; i < 300; i++) {
            month = Month.of((int)(Math.round(Math.random() * 11)+1)).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
            date = String.valueOf(Math.round(Math.random() * 28)) + "-" + month + "-2019";
            time = String.valueOf(Math.round(Math.random() * 23) + 1) + ":" + String.valueOf(Math.round(Math.random() * 60));
            userId = "Hi" + String.valueOf(Math.round(Math.random() * 100));
            lat = ((Math.random() * 0.11751) + 37.717258) * -1;
            lng = (Math.random() * 0.177837) + 175.193019;
            dB = (Math.random() * 70) + 30;

            Data data = new Data(date, time, userId, lat, lng, dB);
            dataList.add(data);
        }
        return dataList;
    }
}
