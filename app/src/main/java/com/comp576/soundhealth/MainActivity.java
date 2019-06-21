package com.comp576.soundhealth;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private TextView location,dateTime;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;
    private ArrayAdapter<Data> adapter;
    private ArrayList<Data> data = new ArrayList<>();
    private ListView dataListView;
    private static int MINUTE=60;
    private int interval = 1;
    private Switch continuousSwitch;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new DataRepository(this);

        setContentView(R.layout.activity_main);
        location = (TextView) findViewById(R.id.location);
        location.setText("new text I put here 'cos I could");

        dateTime = findViewById(R.id.dateTime);
        String date =new SimpleDateFormat("dd-MMM-yyyy kk:mm", Locale.getDefault()).format(new Date());
        dateTime.setText(date);

        //setup navigation drawer stuff
        drawerLayout = (DrawerLayout)findViewById(R.id.activity_main);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.common_open_on_phone,R.string.common_open_on_phone);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView)findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id){
                    case R.id.chart:
                        Intent goToChart = new Intent(getApplicationContext(),ChartActivity.class);
                        startActivity(goToChart);
                        break;
                    case R.id.settings:
                        break;
                    case R.id.mapview:
                        Intent goToMap = new Intent(getApplicationContext(),MapsActivity.class);
                        startActivity(goToMap);
                        break;
                    case R.id.continuousSwitch:
                        if (!((Switch)menuItem.getActionView()).isChecked()){
                            ((Switch)menuItem.getActionView()).setChecked(true);
                            scheduleDataCollection();
                            return true;
                        } else {
                            ((Switch)menuItem.getActionView()).setChecked(false);
                            cancelDataCollection();
                            return true;
                        }
                    default:
                        return true;
                }
                return true;
            }
        });

        continuousSwitch = (Switch)navigationView.getMenu().findItem(R.id.continuousSwitch).getActionView();
        continuousSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    scheduleDataCollection();
                } else {
                    cancelDataCollection();
                }
            }
        });
        //check that app can access location data and record audio
        checkPermissions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions(){
        if (checkSelfPermission(RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{RECORD_AUDIO,ACCESS_FINE_LOCATION},0);
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
    }


    // Setup a recurring alarm every half hour
    //from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
    public void scheduleDataCollection() {
        Toast.makeText(this,"data collection started",Toast.LENGTH_SHORT).show();
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
                interval*MINUTE*1000, pIntent);
    }

    //stop data collection service
    public void cancelDataCollection() {
        Toast.makeText(this,"data collection stopped",Toast.LENGTH_SHORT).show();

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
        DataCollection dataCollectior = new DataCollection(getApplicationContext());
        dataCollectior.getDataPoint();
    }

    public void callSendDataCollection(View view){
        DataCollection dataCollection = new DataCollection(getApplicationContext());
        dataCollection.sendDataCollection();
    }
}
