package com.comp576.soundhealth;

import java.util.ArrayList;
import java.util.List;

//for generating lots of fake data in and around hamilton if I need it.
public class GenerateData {
    String date,time,userId;
    double lat,lng,dB;
    List<Data> dataList=new ArrayList<>();

    public List<Data> getFakeData(){

        for(int i=0;i<100;i++){
            date = String.valueOf(Math.round(Math.random()*28)) + "-Jun-2019";
            time = String.valueOf(Math.round(Math.random()*10)+1)+":" + String.valueOf(Math.round(Math.random()*60)) +" PM";
            userId = "justSomeGuy";
            lat = ((Math.random()*0.11751)+37.717258)*-1;
            lng = (Math.random()*0.177837)+175.193019;
            dB = (Math.random()*70)+30;

            Data data=new Data(date,time,userId,lat,lng,dB);
            dataList.add(data);
        }
        return dataList;
    }
}
