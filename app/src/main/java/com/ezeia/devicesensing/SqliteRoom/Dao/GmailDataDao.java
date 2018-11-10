package com.ezeia.devicesensing.SqliteRoom.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;
import com.ezeia.devicesensing.SqliteRoom.entity.GmailData;

import java.util.List;

@Dao
public interface GmailDataDao {

    @Query("SELECT * FROM gmailData")
    List<GmailData> getAll();

    @Query("SELECT * FROM gmailData LIMIT  :limit")
    List<GmailData> getAll(int limit);

    @Query("DELETE FROM gmailData WHERE MsgID IN (SELECT MsgID FROM gmailData LIMIT :limit)")
    void deleteByLimit(int limit);

    @Query("SELECT COUNT(*) from gmailData")
    int countUsers();

    @Insert
    void insertAll(GmailData... users);

    @Query("DELETE from gmailData")
    void delete();
}