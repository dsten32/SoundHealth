package com.comp576.soundhealth;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
Class to handle the scheduled data collection as set up by the user.
If the user set stop time has been reached then the alarm is cancelled and the UI reset.
Else a DataCollection instance is created and a datapoint logged.
 */
public class DataCollectionService extends IntentService {


    public DataCollectionService() {
        super("DataCollectionService");
    }
/**
On service start, get the current time and compare to the scheduled data collection stop time.
Determine whether to collect data or cancel timer and update UI.
 */
    @Override
    protected void onHandleIntent(Intent intent) {
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
