package com.comp576.soundhealth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.glassfish.jersey.client.ClientConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class DataCollection extends Activity {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DataRepository dataRepository;
    private Context context;
    private FirebaseFirestore db;
    private String addressString;
    private Data data;
    private Data lastData;

    public DataCollection (Context context){
        this.context=context;
        dataRepository = new DataRepository(context);
        db = FirebaseFirestore.getInstance();
        data=null;
    }

    //data collection method move to own class?
    @SuppressLint("MissingPermission") //add an exception try/catch to the getLastLocation?
    public void getDataPoint(){
        //see if we can generate some data shall we?
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String date =new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
                            String time =new SimpleDateFormat("kk:mm", Locale.getDefault()).format(new Date());
                            Log.d("time is", time);
                            String userId = "some text I haven't decided yet";
                            Double lat,lng;
                            //adding a random change to the location data based on user's selected perturbation value 0-0.8km
                            //should probably add a variable to Data pojo to capture if data is perturbed in this fashion.
                            int blurFactor = ((int)(MainActivity.blurValue * 10));
                            if(blurFactor!=0 && MainActivity.isBlurred){
                                lat = (Math.random() * (blurFactor * 0.0005))+(location.getLatitude()-(blurFactor * 0.0005));
                                lng = (Math.random() * (blurFactor * 0.0005))+(location.getLongitude()-(blurFactor * 0.0005));
                            } else {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                            Double dB=null;
                            try {
                                dB = new Recorder().getNoiseLevel();
                            } catch (NoValidNoiseLevelException e) {
                                e.printStackTrace();
                            }

                            data = new Data(date,time,userId,lat,lng,dB,MainActivity.isBlurred);
//                            lastData = new LastDataAsyncTask().doInBackground();

                            long id=dataRepository.insert(data);
                            data.id=id;
                            sendData(data);
                            new AddressAsyncTask().execute(data);
                            Toast.makeText(context, "Datapoint saved: "+(double)Math.round(data.dB*100)/100, Toast.LENGTH_SHORT).show();
//                            Toast.makeText(context,lastData.toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
            } catch (URISyntaxException | ParseException  e) {
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
                    + "<br><b>location Blurred: </b><em>" + String.valueOf(data.isBlurred) + "</em>"
                    + "<br><b>Location: </b><em>" + addressString.replace(",","<br>") + "</em>"
                    + "<br><b>dB: </b><em>" + String.valueOf((Math.round(data.dB))) + "</em>";
            MainActivity.mainButton.setText(Html.fromHtml(htmlButtonText));
        }
    }

    private class LastDataAsyncTask extends AsyncTask<Void,Void,Data>{

        @Override
        protected Data doInBackground(Void... voids) {
            return dataRepository.lastItem();        }
    }



    public void sendData(Data data){
// Add a new document with a generated ID
        db.collection("sound_data_collection")
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendDataCollection(){
        //create a batch of fake data and send to the firestore.
        ArrayList<Data> dataList = (ArrayList<Data>) new GenerateData().getFakeData();
        WriteBatch batch = db.batch();
        int dbID=0; //want to add id to the data as it's being sent. didn't work but not sure if it was 'cos of other problems
        String paddedID;
        for (Data data:dataList) {
            dbID = ((int)(double)(Math.random()*10000));
            paddedID=String.format("%1$" + 4 + "s", dbID).replace(' ', '0');
            DocumentReference newDoc = db.collection("sound_data_collection").document(paddedID);
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
        ArrayList<Data> dataList = new ArrayList<>();

        db.collection("sound_data_collection")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                dataList.add(new Data(document.getString("date"),document.getString("time"),document.getString("userId"),document.getDouble("lat"),document.getDouble("lng"),document.getDouble("dB"),document.getBoolean("isBlurred")));
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
        return dataList;
    }

}
