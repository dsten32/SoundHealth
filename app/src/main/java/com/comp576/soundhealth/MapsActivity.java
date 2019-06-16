package com.comp576.soundhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

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
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;
    private TextView textPlace;
    private List<Data> userDataList = new ArrayList<>();
    private List<Data> allDataList = new ArrayList<>();
    private DataRepository dataRepository;
    private DataCollection dataCollection;
    private Button allDataHeatMap;
    private DialogFragment dialogFragment;
    private Boolean mapUserData = true;
    private String[] daysToMap = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private CheckBox monBox, tueBox, wedBox, thurBox, friBox, satBox, sunBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        textPlace = findViewById(R.id.textPlace);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dataRepository = new DataRepository(this);

        new UserAsyncTask().execute();

        dataCollection = new DataCollection(getApplicationContext());

        new FirebaseAsyncTask().execute();

        monBox = findViewById(R.id.monBox);
        tueBox = findViewById(R.id.tueBox);
        wedBox = findViewById(R.id.wedBox);
        thurBox = findViewById(R.id.thurBox);
        friBox = findViewById(R.id.friBox);
        satBox = findViewById(R.id.satBox);
        sunBox = findViewById(R.id.sunBox);
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
            Log.d(" Heres the userdata:", userDataList.get(0).toString());
            addHeatMap();
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
            allDataList.addAll(data);
            allDataHeatMap = (Button) findViewById(R.id.allDataHeat);
            allDataHeatMap.setEnabled(true);
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

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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
//                            textPlace.setText("Latitude: " + String.valueOf(location.getLatitude()) +"\nLongitude: "+String.valueOf(location.getLongitude()));
//                            Toast.makeText(getApplication().getApplicationContext(), "my location is: "+location, Toast.LENGTH_LONG).show();
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(current).title("you are here"));

//                            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
//                            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//                            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
//                            mMap.setMinZoomPreference(8);
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);
//                            addHeatMap();

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                    .zoom(12)                   // Sets the zoom
                                    .bearing(0)                // Sets the orientation of the camera to North
                                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        }
                    }
                });

    }

    //generate user userDataList heatmap layer an add to the map
    private void addHeatMap() {
        mMap.clear();
        List<WeightedLatLng> list = new ArrayList<>();

// Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green

                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.0f, 1f//0.16666f, 0.33333f, 0.5f, 0.66666f, 0.83333f,
        };

        Gradient gradient = new Gradient(colors, startPoints);

        for (Data data : userDataList) {
            if (data.lati != null && data.longi != null && data.dB != null) {
                list.add(new WeightedLatLng(new LatLng(data.lati, data.longi), ((data.dB - 30) / 10) * 0.16333));
            }
        }

        // Create a heat map tile provider, passing it the latlngs of the datapoints.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(list)
                .gradient(gradient)
                .radius(50)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    //generate heatmap layer for all firestore userDataList
    private void addHeatMapForAllData() {

        mMap.clear();
        List<WeightedLatLng> weightedLatLngs = new ArrayList<>();

// Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.GREEN,    // green(0-50)
                Color.YELLOW,    // yellow(51-100)
                Color.rgb(255, 165, 0), //Orange(101-150)
                Color.RED,              //red(151-200)
                Color.rgb(153, 50, 204), //dark orchid(201-300)
                Color.rgb(165, 42, 42), //brown(301-500)
//                Color.BLUE,
        };

        float[] startPoints = {
                0.0f, 0.16666f, 0.33333f, 0.5f, 0.66666f, 0.83333f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);


        for (Data data : allDataList) {
            if (data.lati != null && data.longi != null && data.dB != null) {
                weightedLatLngs.add(new WeightedLatLng(new LatLng(data.lati, data.longi), ((data.dB - 30) * 10) * 0.16333));
            }
        }

        // Create a heat map tile provider, passing it the latlngs of the datapoints.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(weightedLatLngs)
                .gradient(gradient)
                .radius(50)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    //generate heatmap layer based on filter settings set by user. to replace the other two methods, probably.
    private void addFilteredHeatMap() throws ParseException {
        mMap.clear();
        List<Data> heatMapData = new ArrayList<>();
        List<WeightedLatLng> weightedLatLngs = new ArrayList<>();

// Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.GREEN,    // green(0-50)
                Color.YELLOW,    // yellow(51-100)
                Color.rgb(255, 165, 0), //Orange(101-150)
                Color.RED,              //red(151-200)
                Color.rgb(153, 50, 204), //dark orchid(201-300)
                Color.rgb(165, 42, 42), //brown(301-500)
//                Color.BLUE,
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

        for (Data dataPoint : heatMapData) {
            String day = new SimpleDateFormat("EEEE").format(new SimpleDateFormat("dd-MMM-yyyy").parse(dataPoint.date));
//            Log.d("Date day = ",day);
            if (dataPoint.lati != null && dataPoint.longi != null && dataPoint.dB != null && Arrays.asList(daysToMap).contains(day)) {
                weightedLatLngs.add(new WeightedLatLng(new LatLng(dataPoint.lati, dataPoint.longi), ((dataPoint.dB - 30) * 10) * 0.16333));
            }
        }


        // Create a heat map tile provider, passing it the latlngs of the datapoints.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(weightedLatLngs)
                .gradient(gradient)
                .radius(50)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }


    //go back to the main activity screen
    public void goToMain(View view) {
        Intent goToMain = new Intent(this, MainActivity.class);
        startActivity(goToMain);
    }

    //method for calling the user userDataList heatmap method from the view
    public void callAddHeatMapForUserData(View view) throws ParseException {
//        addHeatMap()
 addFilteredHeatMap();
    }

    //method for calling the all userDataList heatmap method from the view
    public void callAddHeatMapForAllData(View view) {
        addHeatMapForAllData();
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

    //dismis the settings dialog fragment
    public void dismissSettings(View view) {
        dialogFragment.dismiss();
    }

    public void setMapUserData(Boolean mapUserData) {
        this.mapUserData = mapUserData;
    }

    public void setDaysToMap(String[] daysToMap) {
        this.daysToMap = daysToMap;
    }


}
