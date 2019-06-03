package com.comp576.soundhealth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;


public class DataCollection extends Activity {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;
    private Context context;

    public DataCollection (Context context){
        this.context=context;
        dataRepository = new DataRepository(context);
    }

    //data collection method move to own class?
    @SuppressLint("MissingPermission") //add an exception try/catch to the getLastLocation?
    public void getDataPoint(){
        //see if we can generate some data shall we?
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String date =new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
                            String time =new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                            String userId = "some text I haven't decided yet";
                            Double lati = location.getLatitude();
                            Double longi = location.getLongitude();
                            Double dB=null;
                            try {
                                dB = new Recorder().getNoiseLevel();
                            } catch (NoValidNoiseLevelException e) {
                                e.printStackTrace();
                            }
//                            Double dB = (Math.random()*70)+30;

                            dataRepository.insert(new Data(date,time,userId,lati,longi,dB));

//                            textPlace.setText(String.valueOf(location.getLatitude()));
                            Toast.makeText(context, "Datapoint saved: "+String.valueOf(dB), Toast.LENGTH_LONG).show();
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }

}
