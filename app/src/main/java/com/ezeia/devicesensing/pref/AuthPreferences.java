package com.ezeia.devicesensing.pref;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPreferences {
    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "token";
    private static final String MAIL_DATE = "mail_date";
    private static final String MAIL_SENDER = "mail_id";


    private final SharedPreferences preferences;

    public AuthPreferences(Context context) {
        preferences = context
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void setUser(String user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, user);
        editor.commit();
    }

    public void setToken(String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, password);
        editor.commit();
    }

    public void setMailDate(String mailDate) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MAIL_DATE, mailDate);
        editor.apply();
    }

    public void setMailSender(String mailSender) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MAIL_SENDER, mailSender);
        editor.apply();
    }

    public String getUser() {
        return preferences.getString(KEY_USER, null);
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getMailDate() {
        return preferences.getString(MAIL_DATE, null);
    }

    public String getMailSender() { return preferences.getString(MAIL_SENDER, null);}
}
