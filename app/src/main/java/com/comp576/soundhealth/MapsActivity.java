package com.comp576.soundhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;
    private List<Data> userDataList = new ArrayList<>();
    private List<Data> allDataList = new ArrayList<>();
    private DataRepository dataRepository;
    private DataCollection dataCollection;
    private DialogFragment dialogFragment;
    private long millisAllDataRetrieved;
    private Boolean mapUserData = true;
    public String[] daysToMap = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private int startHour = 0, startMin = 0, stopHour = 23, stopMin = 60;
    private int sYear, sMonth, sDay, eYear, eMonth, eDay;
    private DialogFragment spinnerFragment;
    private final Calendar calendar = Calendar.getInstance();
    private DialogFragment datePicker = new HeatmapSettingDialogFragment.DatePickerFragment();
    private DialogFragment timePicker = new HeatmapSettingDialogFragment.TimePickerFragment();
    private HeatmapSettingDialogFragment heatmapDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dataRepository = new DataRepository(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sound Heatmap");

        dataCollection = new DataCollection(getApplicationContext());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    //get user userDataList from repository and add heatmap to map on complete
    private class UserAsyncTask extends AsyncTask<Void, Void, List<Data>> {

        @Override
        protected List<Data> doInBackground(Void... voids) {
            return dataRepository.getDataList();
        }

        @Override
        protected void onPostExecute(List<Data> userData) {
            userDataList.addAll(userData);
            if (userData.size() != 0) {
                try {
                    addFilteredHeatMap();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //get all userDataList from Firestore and enable allDataList Heatmap button on complete
    private class FirebaseAsyncTask extends AsyncTask<Void, Void, List<Data>> {

        @Override
        protected List<Data> doInBackground(Void... voids) {
            return dataCollection.getDataCollection();
        }

        @Override
        protected void onPostExecute(List<Data> data) {
            millisAllDataRetrieved = System.currentTimeMillis();
            allDataList.addAll(data);
            spinnerFragment.dismiss();
            try {
                addFilteredHeatMap();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 0);
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
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(current).title("you are here"));
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                    .zoom(12)                   // Sets the zoom
                                    .bearing(0)                // Sets the orientation of the camera to North
                                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            //Async userdata here???
                            Log.d("userDataList.size = ",String.valueOf(userDataList.size() +" mapud: "+String.valueOf(mapUserData)));

                            if (userDataList.size() == 0 && mapUserData) {
                                Log.d("getting user data","true");
                                new UserAsyncTask().execute();
                            } else if (((System.currentTimeMillis() - millisAllDataRetrieved) > 3600000) && !mapUserData) {
                                showSpinner();
                                new FirebaseAsyncTask().execute();
                            } else {
                                try {
                                    addFilteredHeatMap();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }


    //generate heatmap layer based on filter settings set by user. to replace the other two methods, probably.
    private void addFilteredHeatMap() throws ParseException {
        mMap.clear();
        List<Data> heatMapData = new ArrayList<>();
        List<WeightedLatLng> weightedLatLngs = new ArrayList<>();
        Log.d("adding filt ",String.valueOf(userDataList.size()));

        // Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.GREEN,    // green(0-50)
                Color.YELLOW,    // yellow(51-100)
                Color.rgb(255, 165, 0), //Orange(101-150)
                Color.RED,              //red(151-200)
                Color.rgb(153, 50, 204), //dark orchid(201-300)
                Color.rgb(165, 42, 42), //brown(301-500)
        };

        float[] startPoints = {
                0.0f, 0.16666f, 0.33333f, 0.5f, 0.66666f, 0.83333f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        //check if heatmap should use the users own userDataList
        if (mapUserData) {
            heatMapData.addAll(userDataList);
        } else {
            heatMapData.addAll(allDataList);
        }
        Log.d("heatmapdatasize: ", String.valueOf(heatMapData.size()));

        if (!(sDay + sMonth + sYear > 0 && eDay + eMonth + eYear > 0)) {
            sDay = 1;
            sMonth = 1;
            sYear = 2019;
            eDay = calendar.get(Calendar.DAY_OF_MONTH);
            eMonth = calendar.get(Calendar.MONTH) + 1;
            eYear = calendar.get(Calendar.YEAR);
        }

        String sSDate = sDay + "/" + sMonth + "/" + sYear;
        String sEDate = eDay + "/" + eMonth + "/" + eYear;
        Date dSDate = new SimpleDateFormat("dd/MM/yyyy").parse(sSDate);
        Date dEDate = new SimpleDateFormat("dd/MM/yyyy").parse(sEDate);

        for (Data dataPoint : heatMapData) {
            String datapointDay = null;
            Date datapointDate = null;
            try {
                datapointDay = new SimpleDateFormat("EEEE").format(new SimpleDateFormat("dd-MMM-yyyy").parse(dataPoint.date));
                datapointDate = new SimpleDateFormat("dd-MMM-yyyy").parse(dataPoint.date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String[] datapointTime = dataPoint.time.split(":");
            int datapointMinutes = (Integer.parseInt(datapointTime[0]) * 60) + Integer.parseInt(datapointTime[1]);
            int startTimeMinutes = (startHour * 60) + startMin;
            int stopTimeMinutes = (stopHour * 60) + stopMin;

            if ((dataPoint.lat != null)
                    && (dataPoint.lng != null)
                    && (dataPoint.dB != null)
                    && Arrays.asList(daysToMap).contains(datapointDay)
                    && (datapointMinutes >= startTimeMinutes)
                    && (datapointMinutes <= stopTimeMinutes)
                    && ((datapointDate.compareTo(dSDate) >= 0)
                    && (datapointDate.compareTo(dEDate) <= 0))) {
                weightedLatLngs.add(new WeightedLatLng(new LatLng(dataPoint.lat, dataPoint.lng), ((dataPoint.dB - 30) * 100) * 0.16333));
            }
        }

        if (weightedLatLngs.size() != 0) {
            // Create a heat map tile provider, passing it the latlngs of the datapoints.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(weightedLatLngs)
                    .gradient(gradient)
                    .radius(50)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } else {
            Toast.makeText(getApplicationContext(), "no data that fits the filter found", Toast.LENGTH_SHORT).show();
        }
    }

    //show the heatmap settings dialog fragment
    public void showDialog(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        dialogFragment = new HeatmapSettingDialogFragment();
        dialogFragment.show(fragmentTransaction, "dialog");
    }

    //show the loading spinner dialog fragment
    public void showSpinner() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("spinner");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        spinnerFragment = new SpinnerFragment();
        spinnerFragment.show(fragmentTransaction, "spinner");
    }

    public void showPickerDialog(View v) {
        int id = v.getId();
        heatmapDialog = (HeatmapSettingDialogFragment) getSupportFragmentManager().findFragmentByTag("dialog");
        switch (id) {
            case R.id.sTime:
                heatmapDialog.setTimeFlag(HeatmapSettingDialogFragment.START_FLAG);
                timePicker.show(getSupportFragmentManager(), "startTimePicker");
                break;
            case R.id.eTime:
                heatmapDialog.setTimeFlag(HeatmapSettingDialogFragment.END_FLAG);
                timePicker.show(getSupportFragmentManager(), "stopTimePicker");
                break;
            case R.id.sDate:
                heatmapDialog.setDateFlag(HeatmapSettingDialogFragment.START_FLAG);
                datePicker.show(getSupportFragmentManager(), "startDatePicker");
                break;
            case R.id.eDate:
                heatmapDialog.setDateFlag(HeatmapSettingDialogFragment.END_FLAG);
                datePicker.show(getSupportFragmentManager(), "stopDatePicker");
                break;
            default:
                break;
        }

    }

    //dismiss the settings dialog fragment
    public void dismissSettings(View view) {
        dialogFragment.dismiss();
        if (mapUserData) {
            try {
                addFilteredHeatMap();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if ((System.currentTimeMillis() - millisAllDataRetrieved) > 3600000) {
                showSpinner();
                new FirebaseAsyncTask().execute();
            } else {
                try {
                    addFilteredHeatMap();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //setters&getters
    public void setMapUserData(Boolean mapUserData) {
        this.mapUserData = mapUserData;
    }

    public void setDaysToMap(String[] daysToMap) {
        this.daysToMap = daysToMap;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public void setStopHour(int stopHour) {
        this.stopHour = stopHour;
    }

    public void setStopMin(int stopMin) {
        this.stopMin = stopMin;
    }

    public void setsYear(int sYear) {
        this.sYear = sYear;
    }

    public void setsMonth(int sMonth) {
        this.sMonth = sMonth;
    }

    public void setsDay(int sDay) {
        this.sDay = sDay;
    }

    public void seteYear(int eYear) {
        this.eYear = eYear;
    }

    public void seteMonth(int eMonth) {
        this.eMonth = eMonth;
    }

    public void seteDay(int eDay) {
        this.eDay = eDay;
    }

    public int getsYear() {
        return sYear;
    }

    public int getsMonth() {
        return sMonth;
    }

    public int getsDay() {
        return sDay;
    }

    public int geteYear() {
        return eYear;
    }

    public int geteMonth() {
        return eMonth;
    }

    public int geteDay() {
        return eDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public int getStopHour() {
        return stopHour;
    }

    public int getStopMin() {
        return stopMin;
    }

    public Boolean getMapUserData() {
        return mapUserData;
    }
}
