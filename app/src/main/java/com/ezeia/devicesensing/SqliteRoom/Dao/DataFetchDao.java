package com.ezeia.devicesensing.SqliteRoom.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;

import java.util.List;

@Dao
public interface DataFetchDao {

    @Query("SELECT * FROM datafetch where ProbeName LIKE :ProbeName")
    List<DataFetch> getAll(String ProbeName);

    @Query("SELECT ProbeInfo FROM datafetch where ProbeName LIKE  :ProbeName")
    List<String> findByName(String ProbeName);

    @Query("SELECT TimeStamp FROM datafetch where ProbeInfo LIKE  :ProbeInfo")
    String getPrimaryID(String ProbeInfo);

    @Query("SELECT ProbeInfo FROM datafetch where ProbeName LIKE  :ProbeName")
    String findSingleDataByName(String ProbeName);

    @Query("SELECT ProbeInfo FROM datafetch where ProbeName LIKE  :ProbeName")
    String findFinalDataByName(String ProbeName);

    @Query("DELETE FROM datafetch where ProbeName = :Name")
    void deleteByName(String Name);

    @Query("UPDATE datafetch SET SubmitFlag=:submitFlag WHERE ProbeName = :currentFlg AND TimeStamp = :TimeStamp")
    void update(String submitFlag, String currentFlg,String TimeStamp);

    @Query("SELECT SubmitFlag FROM datafetch where ProbeName LIKE :ProbeName AND TimeStamp = :TimeStamp")
    String getSubmitFlag(String ProbeName,String TimeStamp);

    @Query("SELECT COUNT(*) from datafetch")
    int countUsers();

    @Insert
    void insertAll(DataFetch... users);

    @Query("DELETE from datafetch")
    void delete();
}