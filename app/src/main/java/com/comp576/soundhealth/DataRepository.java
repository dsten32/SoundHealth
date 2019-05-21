package com.comp576.soundhealth;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class DataRepository {
    private DataDao dataDao;
    private LiveData<List<Data>> allData;
    DataRepository(Context context) {
        DataRoomDatabase db = DataRoomDatabase.getDatabase(context);
        dataDao = db.dataDao();
        allData = dataDao.getAllData();
    }
    public void insert(Data data) {
        new InsertAsyncTask().execute(data);
    }
    private class InsertAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(final Data... params) {
            for (Data data : params) {
                dataDao.insert(data);
            }
            return null;
        }
    }
    public void update(Data data) {
        new UpdateAsyncTask().execute(data);
    }
    private class UpdateAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(final Data... params) {
            for (Data data : params) {
                dataDao.update(data);
            }
            return null;
        }
    }
    public void delete(Data data) {
        new DeleteAsyncTask().execute(data);
    }
    private class DeleteAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(final Data... params) {
            for (Data data : params) {
                dataDao.delete(data);
            }
            return null;
        }
    }

    public LiveData<List<Data>> getAllData() {
        return allData;
    }
}