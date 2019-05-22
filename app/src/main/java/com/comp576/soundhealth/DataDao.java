package com.comp576.soundhealth;

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
    void insert(Data data);
    @Update
    void update(Data data);
    @Delete
    void delete(Data data);
    @Query("SELECT * from data")//ORDER BY id")
    LiveData<List<Data>> getAllData();

}
