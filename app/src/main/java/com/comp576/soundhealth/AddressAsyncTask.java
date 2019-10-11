package com.comp576.soundhealth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.Toast;
import android.content.Context;


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

/**
Class to retrieve the address of data points via google api.
Retrieval performed on a background thread and returned to the calling method
 */
public class AddressAsyncTask extends AsyncTask<Data, Void, String> {
    private Data data;
    private Activity activity;
    private boolean updateButtonText;
    private boolean isInternet;

    public AddressAsyncTask(Activity activity, Data data) {
        this.data = data;
        this.activity = activity;
    }
/**
Get network connection and send the latitude and longitude values to the api.
Parse the resulting json to pull out the useful address to pass to the postexecute method
 */
    @Override
    public String doInBackground(Data... data) {
        NetworkInfo activeNetwork = ((ConnectivityManager) activity.getApplication().getApplicationContext()
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
            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            URI apiURI = new URI("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=AIzaSyC8avA0VDGZmk6m1sAI7feb1HCEBDK41BY");
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

    /**
    Take the address obtained and generate a string using the datapoint date, time,
    decibel and blur status.
    If called by the post data collection method format string as html for display.
    else it was called by the bar chart and should show as a Toast
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(String address) {
        String addressString = address;
        if (isInternet) {
            if (updateButtonText) {
                String htmlButtonText = "<br><h5>Most recent noise data</h5>"
                        + "<b>Date: </b><em>" + data.date + "</em>"
                        + "<br><b>Time: </b><em>" + data.time + "</em>"
                        + "<br><b>location Blurred: </b><em>" + String.valueOf(data.isBlurred) + "</em>"
                        + "<br><b>Location: </b><em>" + addressString.replace(",", "<br>") + "</em>"
                        + "<br><b>dB: </b><em>" + String.valueOf((Math.round(data.dB))) + "</em>";
                MainActivity.mainButton.setText(Html.fromHtml(htmlButtonText));
            } else {
                String[] time = data.time.split(":");
                String amPm = "am";
                if (Integer.parseInt(time[0]) >= 12)
                    amPm = "pm";
                Toast.makeText(activity.getApplicationContext(),
                        "Decibel: "
                                + String.valueOf((double) (Math.round(data.dB * 100)) / 100)
                                + "\n" + "Time taken: "
                                + time[0] + ":" + time[1] + " "
                                + amPm
                                + "\n" + addressString.replace(", ", "\n")
                        , Toast.LENGTH_LONG).show();
            }
        } else {
            MainActivity.mainButton.setText(address);
        }
    }

    public void setUpdateButtonText(boolean updateButtonText) {
        this.updateButtonText = updateButtonText;
    }
}
