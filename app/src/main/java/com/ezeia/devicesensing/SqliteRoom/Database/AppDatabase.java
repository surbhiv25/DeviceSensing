package com.ezeia.devicesensing.SqliteRoom.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Dao.DataFetchDao;
import com.ezeia.devicesensing.SqliteRoom.Dao.GmailDataDao;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;
import com.ezeia.devicesensing.SqliteRoom.entity.GmailData;

import io.fabric.sdk.android.Fabric;

@Database(entities = {DataFetch.class, GmailData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract DataFetchDao userDao();
    public abstract GmailDataDao gmailDataDao();

    public static AppDatabase getAppDatabase(Context context) {
        Fabric.with(context, new Crashlytics());
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            //.addMigrations(MIGRATION_1_2)
                            .build();
        }
        return INSTANCE;
    }

   /* static final Migration FROM_1_TO_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE datafetch ADD COLUMN createdAt TEXT");
        }
    };*/

    public static void destroyInstance() {
        INSTANCE = null;
    }
}