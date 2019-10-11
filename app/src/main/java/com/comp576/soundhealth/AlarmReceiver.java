package com.comp576.soundhealth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
Triggered by the Alarm based on user chosen data collection interval. Starts the DataCollectionService.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

    /**
     Creates new intent and passes to DataCollectionService when started.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver","accessed");
        Intent i = new Intent(context, DataCollectionService.class);
            context.startService(i);
    }
}
