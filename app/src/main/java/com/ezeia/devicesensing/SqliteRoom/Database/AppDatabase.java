package com.ezeia.devicesensing.SqliteRoom.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Dao.DataFetchDao;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;

import io.fabric.sdk.android.Fabric;

@Database(entities = {DataFetch.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract DataFetchDao userDao();

    public static AppDatabase getAppDatabase(Context context) {
        Fabric.with(context, new Crashlytics());
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}