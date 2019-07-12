package com.comp576.soundhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.glassfish.jersey.client.ClientConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private TextView introText;
    private static int MINUTE = 60;
    private int interval;
    public static Switch continuousSwitch;
    private DialogFragment dialogFragment;
    public static boolean isBlurred, isStopTime, isCollecting;
    public static float blurValue;
    public static float feedbackRating;
    public static String feedbackText;
    public static int stopHour, stopMin;
    public static Button mainButton;
    private String dataStopTime;
    private DialogFragment timePicker = new DataCollectionSettingsFragment.TimePickerFragment();
    private DataRepository repo;
    private Data data;
    private DataCollection dataCollectior;
    private  String addressString;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = new DataRepository(getApplicationContext());
        setContentView(R.layout.activity_main);
        introText = (TextView) findViewById(R.id.intro);
        dataCollectior = new DataCollection(getApplicationContext());
        interval = 30;
//        introText.setText("new text I put here 'cos I could");
        data = repo.lastItem();
        mainButton = (Button) findViewById(R.id.main_btn);

        new AddressAsyncTask().execute(data);

        mainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Collecting", Toast.LENGTH_SHORT).show();
                dataCollectior.getDataPoint();
            }
        });

        //setup navigation drawer stuff
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.common_open_on_phone, R.string.common_open_on_phone);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.nav);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.chart:
                        Intent goToChart = new Intent(getApplicationContext(), ChartActivity.class);
                        startActivity(goToChart);
                        break;
                    case R.id.settings:
                        showSettingsDialog();
                        break;
                    case R.id.mapview:
                        Intent goToMap = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(goToMap);
                        break;
                    case R.id.continuousSwitch:
                        if (!((Switch) menuItem.getActionView()).isChecked()) {
                            ((Switch) menuItem.getActionView()).setChecked(true);
                            return true;
                        } else {
                            ((Switch) menuItem.getActionView()).setChecked(false);
                            cancelDataCollection();
                            return true;
                        }
                    case R.id.fakedata:
                        DataCollection dataCollection = new DataCollection(getApplicationContext());
                        dataCollection.sendDataCollection();
                    case R.id.feedback:
                        showFeedbackDiaolog();
                        break;
                    default:
                        return true;
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        continuousSwitch = (Switch) navigationView.getMenu().findItem(R.id.continuousSwitch).getActionView();
        continuousSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showSettingsDialog();
                } else {
                    cancelDataCollection();
                }
            }
        });
        //check that app can access location data and record audio
        checkPermissions();
    }

    //get address from google geolocation api using the datapoint latlng
    public class AddressAsyncTask extends AsyncTask<Data, Void, String> {

        @Override
        protected String doInBackground(Data... data) {
            double lat = data[0].lat;
            double lng = data[0].lng;
            String address = null;
            try {
                //one connection method
                ClientConfig config=new ClientConfig();
                Client client = ClientBuilder.newClient(config);
                URI apiURI = new URI("https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&key=AIzaSyC8avA0VDGZmk6m1sAI7feb1HCEBDK41BY");
                WebTarget target = client.target(apiURI);
                String jsonResponse = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(jsonResponse);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray results = (JSONArray) jsonObject.get("results");
                JSONObject jsonObject1 = (JSONObject) parser.parse(results.get(0).toString());
                address = jsonObject1.get("formatted_address").toString();
            } catch (URISyntaxException | ParseException e) {
                e.printStackTrace();
            }
            return address;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String address) {
            addressString = address;
            String htmlButtonText = "<br><h5>Most recent noise data</h5>"
                    + "<b>Date: </b><em>" + data.date + "</em>"
                    + "<br><b>Time: </b><em>" + data.time + "</em>"
                    + "<br><b>location Blurred: </b><<em>" + String.valueOf(data.isBlurred) + "</em>"
                    + "<br><b>Location: </b><em>" + addressString.replace(",","<br>") + "</em>"
                    + "<br><b>dB: </b><em>" + String.valueOf((Math.round(data.dB))) + "</em>"
                    ;
            mainButton.setText(Html.fromHtml(htmlButtonText));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (checkSelfPermission(RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED  || checkSelfPermission(Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, ACCESS_FINE_LOCATION,INTERNET}, 0);
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
    }

    // Setup a recurring alarm for user settable (eventually) number of minutes
    //from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
    public void scheduleDataCollection() {
        isCollecting = true;
        Toast.makeText(this, "data collection started", Toast.LENGTH_SHORT).show();
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
                interval * MINUTE * 1000, pIntent);
    }

    //stop data collection service
    public void cancelDataCollection() {
        isCollecting = false;
        Toast.makeText(this, "data collection stopped", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    //show the data collection settings dialog fragment
    public void showSettingsDialog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dataDialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        dialogFragment = new DataCollectionSettingsFragment();
        dialogFragment.show(fragmentTransaction, "dataDialog");
    }

    public void showFeedbackDiaolog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("feedbackDialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        dialogFragment = new FeedbackFragment();
        dialogFragment.show(fragmentTransaction, "feedbackDialog");
//        FeedbackFragment.newInstance().show(getSupportFragmentManager(), "feedbackDialog");;
    }

    public void showPickerDialog(View view) {
        timePicker.show(getSupportFragmentManager(), "dataStopTimePicker");
    }

    //dismiss the settings dialog fragment
    public void dismissSettings(View view) {
        dialogFragment.dismiss();
    }

    //setters

    public void setStopHour(int stopHour) {
        this.stopHour = stopHour;
    }

    public void setStopMin(int stopMin) {
        this.stopMin = stopMin;
    }

    public void setBlurred(boolean blurred) {
        isBlurred = blurred;
    }

    public void setStopTime(boolean stopTime) {
        isStopTime = stopTime;
    }

    public void setBlurValue(float blurValue) {
        this.blurValue = blurValue;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setDataStopTime(String dataStopTime) {
        this.dataStopTime = dataStopTime;
    }

    //getters
    public boolean isBlurred() {
        return isBlurred;
    }

    public boolean isStopTime() {
        return isStopTime;
    }

    public float getBlurValue() {
        return blurValue;
    }

    public int getStopHour() {
        return stopHour;
    }

    public int getStopMin() {
        return stopMin;
    }

    public int getInterval() {
        return interval;
    }

    public String getDataStopTime() {
        return dataStopTime;
    }


}
