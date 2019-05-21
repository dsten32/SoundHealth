package com.comp576.soundhealth;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Data.class}, version = 1,exportSchema = false)
public abstract class DataRoomDatabase extends RoomDatabase {
    public abstract DataDao dataDao();
    private static DataRoomDatabase INSTANCE;
    public static synchronized DataRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    DataRoomDatabase.class, "data_database").build();
        }
        return INSTANCE;
    }
}
