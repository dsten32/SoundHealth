package com.comp576.soundhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MainActivity extends AppCompatActivity{
    private TextView location,dateTime;
    private Button goToMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;
    private ArrayAdapter<Data> adapter;
    private ArrayList<Data> data = new ArrayList<>();
    private ListView dataListView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new DataRepository(this);

        setContentView(R.layout.activity_main);
        location = (TextView) findViewById(R.id.location);
        location.setText("new text I put here 'cos I could");

        dateTime = findViewById(R.id.dateTime);
        String date =new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date());
//        Date date = Calendar.getInstance().getTime();
        dateTime.setText(date);
        goToMap = (Button) findViewById(R.id.goToMap);

//        getDataPoint();
        checkPermissions();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions(){
        if (checkSelfPermission(RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,new String[]{RECORD_AUDIO},0);
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
//            return;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},0);
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
    }



    //data collection method move to own class?
    @SuppressLint("MissingPermission") //add an exception try/catch to the getLastLocation?
    public void getDataPoint(){
        //see if we can generate some data shall we?
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                            Toast.makeText(getApplication().getApplicationContext(), "Datapoint saved: "+String.valueOf(dB), Toast.LENGTH_LONG).show();
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }

    // Setup a recurring alarm every half hour
    //from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
    public void scheduleDataCollection() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                30*1000, pIntent);
    }

    //stop data collection service
    public void cancelDataCollection() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }


    //Navigation methods
    public void goToChart(View view){
        Intent goToChart = new Intent(this,ChartActivity.class);
        startActivity(goToChart);
    }

    public void goToMap(View view){
        Intent goToMap = new Intent(this,MapsActivity.class);
        startActivity(goToMap);
    }


    public void callScheduleDataCollection(View view) {
        scheduleDataCollection();
    }

    public void callCancelDataCollection(View view){
        cancelDataCollection();
    }

    public void callGetDatapoint(View view){
        getDataPoint();
    }
}
