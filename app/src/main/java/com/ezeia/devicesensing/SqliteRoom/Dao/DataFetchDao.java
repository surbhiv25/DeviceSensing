package com.ezeia.devicesensing.SqliteRoom.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;

import org.json.JSONObject;

import java.util.List;

@Dao
public interface DataFetchDao {

    @Query("SELECT * FROM datafetch where ProbeName LIKE :ProbeName")
    List<DataFetch> getAll(String ProbeName);

    @Query("SELECT ProbeInfo FROM datafetch where ProbeName LIKE  :ProbeName")
    List<String> findByName(String ProbeName);

    @Query("SELECT ProbeInfo FROM datafetch where ProbeName LIKE  :ProbeName")
    String findSingleDataByName(String ProbeName);

    @Query("DELETE FROM datafetch where ProbeName = :Name")
    void deleteByName(String Name);

    @Query("SELECT COUNT(*) from datafetch")
    int countUsers();

    @Insert
    void insertAll(DataFetch... users);

    @Delete
    void delete(DataFetch user);
}