package com.ezeia.devicesensing.utils;

import android.os.AsyncTask;

import com.ezeia.devicesensing.service.awstask.AwsUploader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.crashlytics.android.Crashlytics.log;

public class NetworkConnection extends AsyncTask<Void,Void,Boolean>{

    private final AwsUploader uploader;

    public NetworkConnection(AwsUploader uploader){
        this.uploader = uploader;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            HttpURLConnection urlc = (HttpURLConnection)(new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(10000);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            log("IOException in connectGoogle())");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);

        ResultListener listener = uploader;
        listener.isInternetConnected(aVoid);
    }

    public interface ResultListener{
        void isInternetConnected(Boolean aVoid);
    }
}
