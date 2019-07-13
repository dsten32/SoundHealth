package com.comp576.soundhealth;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

//from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
public class DataCollectionService extends IntentService {


    public DataCollectionService() {
        super("DataCollectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("DataCollection","accessed");
        // Do the task here
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        //checks if stop time has been reached and stops schedule, else takes datapoint measurements.
        if(MainActivity.stopHour<=hour && MainActivity.stopMin<=minute) {
            Intent cancelint = new Intent(getApplicationContext(), AlarmReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                    cancelint, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pIntent);
            MainActivity.continuousSwitch.setChecked(false);
            MainActivity.continuousSwitch.jumpDrawablesToCurrentState();

            Log.d("scheduled datapoint","cancelled");
        } else {
            Log.i("DataCollectionService", "Service running" +"hour: "+String.valueOf(MainActivity.stopHour));
            DataCollection dataCollectior = new DataCollection(getApplicationContext());
            dataCollectior.getDataPoint();
            Log.d("scheduled datapoint","collected");
        }
    }
}
