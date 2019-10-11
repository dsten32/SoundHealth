package com.comp576.soundhealth;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.lifecycle.LiveData;

/**
 * Interacts with dao
 */
public class DataRepository {
    private DataDao dataDao;
    private LiveData<List<Data>> allData;
    DataRepository(Context context) {
        DataRoomDatabase db = DataRoomDatabase.getDatabase(context);
        dataDao = db.dataDao();
        allData = dataDao.getAllData();
    }
    public long insert(final Data data) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Callable<Long> insertCallable = () -> dataDao.insert(data);
        long rowId = 0;

        Future<Long> future = executorService.submit(insertCallable);
        try {
            rowId = future.get();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return rowId;
    }


    public List<Data> getDataList(){
        return new DataListAsyncTask().doInBackground();
    }
    private class DataListAsyncTask extends AsyncTask<Void,Void,List<Data>>{

        @Override
        protected List<Data> doInBackground(Void... voids) {
            return dataDao.getDataList();
        }
    }


    public Data lastItem() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Callable<Data> insertCallable = () -> dataDao.getLast();
        Data data = null;

        Future<Data> future = executorService.submit(insertCallable);
        try {
            data = future.get();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return data;
    }

    public Cursor getCursor(){
        Log.d("cursor","getting..");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Callable<Cursor> insertCallable = () -> dataDao.getCursor();
        Cursor cursor = null;

        Future<Cursor> future = executorService.submit(insertCallable);
        try {
            cursor = future.get();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return cursor;
    }
}