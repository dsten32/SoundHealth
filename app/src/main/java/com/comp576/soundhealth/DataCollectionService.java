package com.comp576.soundhealth;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

//from https://github.com/codepath/android_guides/wiki/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks
public class DataCollectionService extends IntentService {


    public DataCollectionService() {
        super("DataCollectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do the task here
        Log.i("DataCollectionService", "Service running");
        DataCollection dataCollectior = new DataCollection(getApplicationContext());
        dataCollectior.getDataPoint();
    }
}
