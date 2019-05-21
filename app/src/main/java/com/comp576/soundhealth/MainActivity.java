package com.comp576.soundhealth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity{
    private TextView location,dateTime;
    private Button goToMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;

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

        getDataPoint();
    }

    //data collection method
    private void getDataPoint(){
        //see if we can generate some data shall we?
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                            Double dB = 45.6;

                            Data dataPoint = new Data(date,time,userId,lati,longi,dB);
                            dataRepository = new DataRepository(getApplication().getApplicationContext());
                            dataRepository.insert(dataPoint);

//                            textPlace.setText(String.valueOf(location.getLatitude()));
                            Toast.makeText(getApplication().getApplicationContext(), "Datapoint saved: "+dataPoint.toString(), Toast.LENGTH_LONG).show();
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
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


}
