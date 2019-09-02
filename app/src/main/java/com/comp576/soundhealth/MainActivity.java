package com.comp576.soundhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.glassfish.jersey.client.ClientConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private static int MINUTE = 60;
    private int interval;
    private DialogFragment dialogFragment;
    private String dataStopTime,addressString;
    private DialogFragment timePicker = new DataCollectionSettingsFragment.TimePickerFragment();
    private DataRepository repo;
    private Data data;
    private Context context;
    private DataCollection dataCollector;
    public static Switch continuousSwitch;
    public static boolean isBlurred, isStopTime, isCollecting;
    public static float blurValue,feedbackRating;
    public static int stopHour = 24, stopMin=60;
    public static Button mainButton;
    //export and share variables
    public static String shareMessage="";
    private Exporter exporter;
    public File file;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        repo = new DataRepository(context);

        setContentView(R.layout.activity_main);

        interval = 30;
        mainButton = (Button) findViewById(R.id.main_btn);
        //get the last item in the database to populate the main button. if null then make something up.
        data = repo.lastItem();
        if (data == null){
            data = new Data("01-Jan-1900", "01:30", "astring", 53.678361, -1.688494, 31.0,false);
        }

        //attempting to error handle no internet connection
        NetworkInfo activeNetwork =((ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if(activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting()) {
            new AddressAsyncTask().execute(data);
        } else {
            mainButton.setText("No internet connection found");
        }

        mainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context, "Collecting", Toast.LENGTH_SHORT).show();
                dataCollector = new DataCollection(context);
                dataCollector.getDataPoint();
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
                        Intent goToChart = new Intent(context, ChartActivity.class);
                        startActivity(goToChart);
                        break;
                    case R.id.settings:
                        showSettingsDialog();
                        break;
                    case R.id.mapview:
                        Intent goToMap = new Intent(context, MapsActivity.class);
                        startActivity(goToMap);
                        break;
                    case R.id.continuousSwitch:
                        if (!((Switch) menuItem.getActionView()).isChecked()) {
                            ((Switch) menuItem.getActionView()).setChecked(true);
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            return true;
                        } else {
                            ((Switch) menuItem.getActionView()).setChecked(false);
                            cancelDataCollection();
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            return true;
                        }
                    case R.id.fakedata:
                        DataCollection dataCollection = new DataCollection(context);
                        dataCollection.sendDataCollection();
                    case R.id.export:
                        exportCSV();
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
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    cancelDataCollection();
//                    ((Switch)navigationView.getMenu().findItem(R.id.continuousSwitch).getActionView()).setChecked(false);
                }
            }
        });
        //check that app can access location data and record audio
        checkPermissions();
//        Cursor cursor = repo.getCursor();

    }

    //get address from google geolocation api using the datapoint latlng
    public class AddressAsyncTask extends AsyncTask<Data, Void, String> {
        boolean isInternet=false;

        @Override
        public String doInBackground(Data... data) {
            NetworkInfo activeNetwork =((ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            isInternet = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isInternet) {
                return "No internet connection found";
            }
            double lat = data[0].lat;
            double lng = data[0].lng;
            String address = null;
            try {
                //one connection method
                ClientConfig config=new ClientConfig();
                Client client = ClientBuilder.newClient(config);
                URI apiURI = new URI("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                        +lat+","+lng
                        +"&key=AIzaSyC8avA0VDGZmk6m1sAI7feb1HCEBDK41BY"); //should really have this secured
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
            if(isInternet) {
                addressString = address;
                String htmlButtonText = "<br><h5>Most recent noise data</h5>"
                        + "<b>Date: </b><em>" + data.date + "</em>"
                        + "<br><b>Time: </b><em>" + data.time + "</em>"
                        + "<br><b>location Blurred: </b><<em>" + String.valueOf(data.isBlurred) + "</em>"
                        + "<br><b>Location: </b><em>" + addressString.replace(",", "<br>") + "</em>"
                        + "<br><b>dB: </b><em>" + String.valueOf((Math.round(data.dB))) + "</em>";
                mainButton.setText(Html.fromHtml(htmlButtonText));
            }else {
                mainButton.setText(address);
            }
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
            return;
        }
    }

    public void exportCSV(){
        exporter = new Exporter(repo,context);
        file = exporter.saveCSV();
        List<String[]> csvData = exporter.getCSV();

        for(int row=0;row<7;row++){
            shareMessage += Arrays.toString(csvData.get(row)) +"\n";
        }
        showShareDialog();
    }

    public void shareCSV(){
        Intent sendIntent = new Intent();
        Uri apkURI = FileProvider.getUriForFile(
                context,
                context
                        .getPackageName() + ".provider", file);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, apkURI);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,"SoundHealth Data");
        startActivity(sendIntent);
    }

    // Setup a recurring alarm for user settable (eventually) number of minutes
    //from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
    public void scheduleDataCollection() {
        isCollecting = true;
        Toast.makeText(this, "data collection started", Toast.LENGTH_SHORT).show();
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
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
        Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    //show dialogs
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

    public void showShareDialog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("shareDialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        dialogFragment = new FeedbackFragment();
        dialogFragment.show(fragmentTransaction, "shareDialog");
//        FeedbackFragment.newInstance().show(getSupportFragmentManager(), "shareDialog");;
    }
    //dismiss the settings dialog fragment
    public void dismissSettings(View view) {
        dialogFragment.dismiss();
    }

    //pickers & setters
    public void showPickerDialog(View view) {
        timePicker.show(getSupportFragmentManager(), "dataStopTimePicker");
    }

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
