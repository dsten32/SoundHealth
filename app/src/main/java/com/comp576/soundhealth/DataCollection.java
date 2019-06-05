package com.comp576.soundhealth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class DataCollection extends Activity {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;
    private Context context;
    private FirebaseFirestore db;

    public DataCollection (Context context){
        this.context=context;
        dataRepository = new DataRepository(context);
        db = FirebaseFirestore.getInstance();
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

                            Data data =new Data(date,time,userId,lati,longi,dB);

                            long id=dataRepository.insert(data);
                            data.id=id;
                            sendData(data);

//                            textPlace.setText(String.valueOf(location.getLatitude()));
                            Toast.makeText(context, "Datapoint saved: "+data.toString(), Toast.LENGTH_LONG).show();
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }

    public void sendData(Data data){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Add a new document with a generated ID
        db.collection("data_collection")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void sendDataCollection(){
        //create a batch of fake data and send to the firestore.
        ArrayList<Data> dataList = (ArrayList<Data>) new GenerateData().getFakeData();
        WriteBatch batch = db.batch();
        for (Data data:dataList) {
            DocumentReference newDoc = db.collection("data_collection").document();
            batch.set(newDoc, data);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // ...
            }
        });
    }

    //get all data from firestore
    public ArrayList<Data> getDataCollection(){
        Log.i(TAG, "Accessed GET ALL DATA");
        ArrayList<Data> dataList = new ArrayList<>();



        db.collection("data_collection")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                dataList.add(new Data(document.getString("date"),document.getString("time"),document.getString("userId"),document.getDouble("lati"),document.getDouble("longi"),document.getDouble("dB")));

//                                Log.d(TAG,dataList.get(dataList.size()-1).toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"LAST TIME"+dataList.get(dataList.size()-1).toString());
        return dataList;
    }

}