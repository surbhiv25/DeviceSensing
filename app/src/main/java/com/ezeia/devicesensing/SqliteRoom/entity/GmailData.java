package com.ezeia.devicesensing.SqliteRoom.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "gmailData")
public class GmailData {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "MsgID")
    private String MsgID;

    public String getMailThreadID() {
        return MailThreadID;
    }

    public void setMailThreadID(String mailThreadID) {
        MailThreadID = mailThreadID;
    }

    @ColumnInfo(name = "MailThreadID")
    private String MailThreadID;

    @ColumnInfo(name = "Date")
    private String Date;

    @ColumnInfo(name = "From")
    private String From;

    @ColumnInfo(name = "To")
    private String To;

    @ColumnInfo(name = "Category")
    private String Category;

    @ColumnInfo(name = "Subject")
    private String Subject;

    @ColumnInfo(name = "Snippet")
    private String Snippet;

    public String getMsgID() {
        return MsgID;
    }

    public void setMsgID(String msgID) {
        MsgID = msgID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getSnippet() {
        return Snippet;
    }

    public void setSnippet(String snippet) {
        Snippet = snippet;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

}