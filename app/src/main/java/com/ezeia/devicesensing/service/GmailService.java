package com.ezeia.devicesensing.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.AuthPreferences;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GmailService extends IntentService {

    private final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private AuthPreferences authPreferences;

    public GmailService(String name) {
        super(name);
    }

    public GmailService() {
        super("GmailService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        authPreferences = new AuthPreferences(this);
        doCoolAuthenticatedStuff();
    }

    private void doCoolAuthenticatedStuff() {
        Account userAccount = null;
        String user = authPreferences.getUser();
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
        for (Account account : accounts) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }

        String token = null;
        try {
            token = GoogleAuthUtil.getToken(this,userAccount, "oauth2:" + SCOPE);
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }
        if (token != null) {
            authPreferences.setToken(token);
            GoogleCredential credential = new GoogleCredential().setAccessToken(token);
            JsonFactory jsonFactory = new JacksonFactory();
            HttpTransport httpTransport = new NetHttpTransport();

            Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("GmailApiTest").build();
            ListMessagesResponse messagesRespose;
            List<Message> m ;

            ArrayList<String> ids = new ArrayList<>();
            ids.add("INBOX");
            try {
                messagesRespose = service.users().messages().list(authPreferences.getUser()).setLabelIds(ids).execute();
                m = messagesRespose.getMessages();
                for(com.google.api.services.gmail.model.Message message : m){
                    String msgID = message.getId();
                    String mailThreadID = message.getThreadId();
                    com.google.api.services.gmail.model.Message messageNew = service.users().messages().get(authPreferences.getUser(), msgID).execute();

                    List<String> list = messageNew.getLabelIds();
                    StringBuilder  builder = new StringBuilder();
                    for(String labelName : list){
                        Log.i("TAG_LABEL",labelName);
                        builder.append(labelName).append(",");
                    }

                    List<MessagePartHeader> headerList = messageNew.getPayload().getHeaders();

                    String fromSender = "", subject = "", toReceiver = "", dateReceived = "";
                    Boolean isFirstMail = true, isFirstMailDate = true;
                    AuthPreferences authPreferences = new AuthPreferences(this);

                    for(MessagePartHeader val: headerList){
                        if(val.getName().equals("From")){
                            fromSender = val.getValue();
                            if(isFirstMail){
                                isFirstMail = false;
                                authPreferences.setMailSender(fromSender);
                            }
                        }
                        if(val.getName().equals("Subject")){
                            subject = val.getValue();
                        }
                        if(val.getName().equals("To")){
                            toReceiver = val.getValue();
                        }
                        if(val.getName().equals("Date")){
                            dateReceived = val.getValue();
                            if(isFirstMailDate){
                                isFirstMailDate = false;
                                authPreferences.setMailDate(dateReceived);
                            }
                        }
                    }
                    String messageSnippet = messageNew.getSnippet();
                    DatabaseInitializer.addGmailData(AppDatabase.getAppDatabase(this),fromSender,toReceiver,dateReceived,
                            builder.toString(),messageSnippet,msgID,subject, mailThreadID);
                    Log.i("TAG_GMAIL","Mail is from: "+fromSender+" and Message ID is: "+msgID+" and subject is:"+subject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        /*    ListLabelsResponse listResponse = null;
            try {
                listResponse = service.users().labels().list(user).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Label> labels = listResponse.getLabels();
            if (labels.isEmpty()) {
                System.out.println("No labels found.");
            } else {
                System.out.println("Labels:");
                for (Label label : labels) {
                    Log.i("TAG","Labels--- "+label.getName());
                    //System.out.printf("- %s\n", label.getName());
                }
            }*/
        }else{
            Log.i("TAG","Token not fetched.");
        }
    }



}
