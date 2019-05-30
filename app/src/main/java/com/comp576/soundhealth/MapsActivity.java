package com.comp576.soundhealth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;


import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;
    private TextView textPlace;
    private List<Data> data = new ArrayList<>();
    private DataRepository dataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textPlace = findViewById(R.id.textPlace);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dataRepository = new DataRepository(this);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                data.addAll(dataRepository.getDataList());
            }
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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
                            textPlace.setText("Latitude: " + String.valueOf(location.getLatitude()) +"\nLongitude: "+String.valueOf(location.getLongitude()));
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
                            addHeatMap();

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

    //generate heatmap layer an add to the map
    private void addHeatMap() {
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

        for (Data data : data){
//            list.add(new WeightedLatLng(new LatLng(data.lati, data.longi),((data.dB - 30) /10)*0.16333));
            list.add(new WeightedLatLng(new LatLng(data.lati, data.longi),20));

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

    public void goToMain(View view){
        Intent goToMain = new Intent(this,MainActivity.class);
        startActivity(goToMain);
    }
}
