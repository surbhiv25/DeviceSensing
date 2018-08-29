package com.ezeia.devicesensing;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogsUtil {

    Context ctx;

    public LogsUtil(Context ctx) {
        this.ctx = ctx;
    }

   /* public void readLogs() {
        StringBuilder logBuilder = new StringBuilder();
        try {
            Process mLogcatProc = null;
            BufferedReader reader = null;
            mLogcatProc = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});

            reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));

            String line;
            final StringBuilder log = new StringBuilder();
            String separator = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                log.append(line);
                log.append(separator);
            }
            String w = log.toString();
            Toast.makeText(ctx.getApplicationContext(), w, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ctx.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/


           private static final String processId = Integer.toString(android.os.Process
                   .myPid());

           public static StringBuilder readCrashLogs() {
               StringBuilder logBuilder = new StringBuilder();
               try {
                   String[] command = new String[] { "logcat", "-d" };
                   Process process = new ProcessBuilder()
                           .command("sh")
                           .redirectErrorStream(true)
                           .start();
                   //Process process = Runtime.getRuntime().exec(command);
                   BufferedReader bufferedReader = new BufferedReader(
                           new InputStreamReader(process.getInputStream()));

                   String line;
                   while ((line = bufferedReader.readLine()) != null) {
                       //if (line.contains(processId)) {
                           logBuilder.append(line);
                           //Code here
                      // }
                   }
               } catch (IOException e) {
               }
               return logBuilder;
           }

           public void checkSysLogs(){
               String pname = ctx.getPackageName();
               String[] CMDLINE_GRANTPERMS = { "su", "-c", null };
               if (ctx.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname) != PackageManager.PERMISSION_DENIED) {
                   Log.d("CRASH", "we do not have the READ_LOGS permission!");
                   if (android.os.Build.VERSION.SDK_INT >= 16) {
                       Log.d("CRASH", "Working around JellyBeans 'feature'...");
                       try {
                           // format the commandline parameter
                           CMDLINE_GRANTPERMS[2] = String.format("pm grant %s android.permission.READ_LOGS", pname);
                           java.lang.Process p = Runtime.getRuntime().exec(CMDLINE_GRANTPERMS);
                           int res = p.waitFor();
                           Log.d("CRASH", "exec returned: " + res);
                           if (res != 0)
                               throw new Exception("failed to become root");
                       } catch (Exception e) {
                           Log.d("CRASH", "exec(): " + e);
                           Toast.makeText(ctx, "Failed to obtain READ_LOGS permission", Toast.LENGTH_LONG).show();
                       }
                   }
               } else
                   Log.d("CRASH", "we have the READ_LOGS permission already!");
           }

    public void readLogs() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -b main -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
           Log.i("TAGHHH",log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}