package com.comp576.soundhealth;

import android.database.Cursor;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DataDao {
    @Insert
    long insert(Data data);
    @Update
    void update(Data data);
    @Delete
    void delete(Data data);
    @Query("SELECT * from Data")//ORDER BY id")
    LiveData<List<Data>> getAllData();
    @Query("SELECT * FROM Data")
    List<Data> getDataList();
    @Query("SELECT * from Data where id=(SELECT count(id) from Data)")
    Data getLast();
    @Query("SELECT * FROM Data")
    Cursor getCursor();
}
