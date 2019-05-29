package com.comp576.soundhealth;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.lifecycle.LiveData;

public class DataRepository {
    private DataDao dataDao;
    private List<Data> dataList;
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

    public LiveData<List<Data>> getAllData() {
        return allData;
    }
}