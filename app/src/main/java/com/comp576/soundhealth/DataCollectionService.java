package com.comp576.soundhealth;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.View;

public class DataCollectionService extends IntentService {


    public DataCollectionService() {
        super("DataCollectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do the task here
        Log.i("DataCollectionService", "Service running");
        MainActivity main = new MainActivity();
        main.getDataPoint();
    }
}
