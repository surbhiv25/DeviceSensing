package com.ezeia.devicesensing.SqliteRoom.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "datafetch")
public class DataFetch {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "ProbeName")
    private String ProbeName;

    @ColumnInfo(name = "ProbeInfo")
    private String ProbeInfo;

    public String getSubmitFlag() {
        return SubmitFlag;
    }

    public void setSubmitFlag(String submitFlag) {
        SubmitFlag = submitFlag;
    }

    @ColumnInfo(name = "SubmitFlag")
    private String SubmitFlag;

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    @ColumnInfo(name = "TimeStamp")
    private String TimeStamp;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getProbeName() {
        return ProbeName;
    }

    public void setProbeName(String probeName) {
        ProbeName = probeName;
    }

    public String getProbeInfo() {
        return ProbeInfo;
    }

    public void setProbeInfo(String probeInfo) {
        ProbeInfo = probeInfo;
    }

}