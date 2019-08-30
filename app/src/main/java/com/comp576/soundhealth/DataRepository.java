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

public class DataRepository {
    private DataDao dataDao;
    private ExecutorService executorService;
    private List<Data> dataList;
    private LiveData<List<Data>> allData;
    DataRepository(Context context) {
        DataRoomDatabase db = DataRoomDatabase.getDatabase(context);
        dataDao = db.dataDao();
        allData = dataDao.getAllData();
    }
    public long insert(final Data data) {
//        InsertAsyncTask insert = new InsertAsyncTask();
//        insert.execute(data);
//        return insert.id;
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

    public List<Data> getDataList(){
//        List<Data> testList = new ArrayList<>();
//        testList.add(new Data("d","t","u",1.0,2.0,3.0));
//
//        return testList;
//        return dataDao.getDataList();
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