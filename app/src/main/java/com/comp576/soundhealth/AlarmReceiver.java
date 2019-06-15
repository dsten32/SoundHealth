package com.comp576.soundhealth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, DataCollectionService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
