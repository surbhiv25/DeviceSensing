package com.ezeia.devicesensing.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GmailService extends IntentService {

    private final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private AuthPreferences authPreferences;
    private String comingFrom = "";

    public GmailService(String name) {
        super(name);
    }

    public GmailService() {
        super("GmailService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        authPreferences = new AuthPreferences(this);
        if (intent != null && intent.getStringExtra("comingFrom") != null && intent.getStringExtra("comingFrom").equalsIgnoreCase("Report")) {
            comingFrom = intent.getStringExtra("comingFrom");
            doCoolStuffAfterReportSent();
        } else {
            doCoolStuffAfterReportSent();
        }
    }

    private void doCoolStuffAfterReportSent() {
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
       /* if (authPreferences != null && authPreferences.getToken() != null && !authPreferences.getToken().equalsIgnoreCase("NA")) {
            token = authPreferences.getToken();
        } else {*/
            try {
                token = GoogleAuthUtil.getToken(this, userAccount, "oauth2:" + SCOPE);
            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
            }
        //}

        if (token != null) {
            authPreferences.setToken(token);
            GoogleCredential credential = new GoogleCredential().setAccessToken(token);
            JsonFactory jsonFactory = new JacksonFactory();
            HttpTransport httpTransport = new NetHttpTransport();

            Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("GmailApiTest").build();
            ListMessagesResponse messagesRespose;
            List<Message> m = null;
            ArrayList<String> ids = new ArrayList<>();
            ids.add("INBOX");

            if (TextUtils.isEmpty(comingFrom)) {
                StringBuilder builderNew = new StringBuilder();
                builderNew.append("after:")
                        .append(CommonFunctions.fetchDateGmail())
                        .append(" ")
                        .append("before:")
                        .append(CommonFunctions.fetchTomorrowDateGmail());

                try {
                    messagesRespose = service.users().messages().list(authPreferences.getUser())
                            .setLabelIds(ids)
                            .setQ(builderNew.toString())
                            .execute();
                    m = messagesRespose.getMessages();
                    Boolean isFirstMail = true, isFirstMailDate = true;
                    for (com.google.api.services.gmail.model.Message message : m) {
                        String msgID = message.getId();
                        String mailThreadID = message.getThreadId();
                        com.google.api.services.gmail.model.Message messageNew = service.users().messages().get(authPreferences.getUser(), msgID).execute();

                        List<String> list = messageNew.getLabelIds();
                        StringBuilder builder = new StringBuilder();
                        for (String labelName : list) {
                            Log.i("TAG_LABEL", labelName);
                            builder.append(labelName).append(",");
                        }

                        List<MessagePartHeader> headerList = messageNew.getPayload().getHeaders();

                        String fromSender = "", subject = "", toReceiver = "", dateReceived = "";
                        AuthPreferences authPreferences = new AuthPreferences(this);

                        for (MessagePartHeader val : headerList) {
                            if (val.getName().equals("From")) {
                                fromSender = val.getValue();
                                if (isFirstMail) {
                                    isFirstMail = false;
                                    authPreferences.setMailSender(fromSender);
                                }
                            }
                            if (val.getName().equals("Subject")) {
                                subject = val.getValue();
                            }
                            if (val.getName().equals("To")) {
                                toReceiver = val.getValue();
                            }
                            if (val.getName().equals("Date")) {
                                if(val.getValue().contains(",")){
                                    String splitDate = val.getValue().split(Pattern.quote(","))[1];
                                    if(splitDate.contains("+")){
                                        dateReceived = splitDate.substring(0,splitDate.indexOf("+")).trim();
                                    }
                                }
                                if (isFirstMailDate) {
                                    isFirstMailDate = false;
                                    authPreferences.setMailDate(dateReceived);
                                }
                            }
                        }
                        String messageSnippet = messageNew.getSnippet();
                        DatabaseInitializer.addGmailData(AppDatabase.getAppDatabase(this), fromSender, toReceiver, dateReceived,
                                builder.toString(), messageSnippet, msgID, subject, mailThreadID);
                        Log.i("TAG_GMAIL", "Mail is from: " + fromSender + " and Date is: " + dateReceived + " and subject is:" + subject);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    messagesRespose = service.users().messages().list(authPreferences.getUser())
                            .setLabelIds(ids)
                            .execute();
                    m = messagesRespose.getMessages();
                    Boolean isFirstMail = true, isFirstMailDate = true;
                    for (com.google.api.services.gmail.model.Message message : m) {
                        String msgID = message.getId();
                        String mailThreadID = message.getThreadId();
                        com.google.api.services.gmail.model.Message messageNew = service.users().messages().get(authPreferences.getUser(), msgID).execute();

                        List<String> list = messageNew.getLabelIds();
                        StringBuilder builder = new StringBuilder();
                        for (String labelName : list) {
                            Log.i("TAG_LABEL", labelName);
                            builder.append(labelName).append(",");
                        }

                        List<MessagePartHeader> headerList = messageNew.getPayload().getHeaders();

                        String fromSender = "", subject = "", toReceiver = "", dateReceived = "";
                        String formattedDate = "", currentDate = "";
                        AuthPreferences authPreferences = new AuthPreferences(this);

                        //last saved date
                        formattedDate = authPreferences.getMailDate();

                        currentDate = CommonFunctions.fetchDayDateTime();

                        for (MessagePartHeader val : headerList) {
                            if (val.getName().equals("From")) {
                                fromSender = val.getValue();
                                if (isFirstMail) {
                                    isFirstMail = false;
                                    authPreferences.setMailSender(fromSender);
                                }
                            }
                            if (val.getName().equals("Subject")) {
                                subject = val.getValue();
                            }
                            if (val.getName().equals("To")) {
                                toReceiver = val.getValue();
                            }
                            if (val.getName().equals("Date")) {
                                if(val.getValue().contains(",")){
                                    String splitDate = val.getValue().split(Pattern.quote(","))[1];
                                    if(splitDate.contains("+")){
                                        dateReceived = splitDate.substring(0,splitDate.indexOf("+")).trim();
                                    }
                                }
                            }
                        }
                        String messageSnippet = messageNew.getSnippet();
                        if(!TextUtils.isEmpty(formattedDate) && !TextUtils.isEmpty(dateReceived)){
                            Boolean check = CommonFunctions.dateCompare(formattedDate.trim(),dateReceived.trim());
                            if(check){
                                if (isFirstMailDate) {
                                    isFirstMailDate = false;
                                    authPreferences.setMailDate(dateReceived);
                                }
                                DatabaseInitializer.addGmailData(AppDatabase.getAppDatabase(this), fromSender, toReceiver, dateReceived,
                                        builder.toString(), messageSnippet, msgID, subject, mailThreadID);
                                Log.i("TAG_GMAIL", "Mail is from: " + fromSender + " and Date is: " + dateReceived + " and subject is:" + subject);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } else {
            Log.i("TAG", "Token not fetched.");
        }
    }

    private void doCoolStuff() {
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
            token = GoogleAuthUtil.getToken(this, userAccount, "oauth2:" + SCOPE);
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
            List<Message> m;
            StringBuilder builderNew = new StringBuilder();
            builderNew.append("after:")
                    .append(CommonFunctions.fetchDateGmail())
                    .append(" ")
                    .append("before:")
                    .append(CommonFunctions.fetchTomorrowDateGmail());

            ArrayList<String> ids = new ArrayList<>();
            ids.add("INBOX");
            try {
                messagesRespose = service.users().messages().list(authPreferences.getUser())
                        .setLabelIds(ids)
                        .setQ(builderNew.toString())
                        .execute();
                m = messagesRespose.getMessages();
                for (com.google.api.services.gmail.model.Message message : m) {
                    String msgID = message.getId();
                    String mailThreadID = message.getThreadId();
                    com.google.api.services.gmail.model.Message messageNew = service.users().messages().get(authPreferences.getUser(), msgID).execute();

                    List<String> list = messageNew.getLabelIds();
                    StringBuilder builder = new StringBuilder();
                    for (String labelName : list) {
                        Log.i("TAG_LABEL", labelName);
                        builder.append(labelName).append(",");
                    }

                    List<MessagePartHeader> headerList = messageNew.getPayload().getHeaders();

                    String fromSender = "", subject = "", toReceiver = "", dateReceived = "";
                    Boolean isFirstMail = true, isFirstMailDate = true;
                    AuthPreferences authPreferences = new AuthPreferences(this);

                    for (MessagePartHeader val : headerList) {
                        if (val.getName().equals("From")) {
                            fromSender = val.getValue();
                            if (isFirstMail) {
                                isFirstMail = false;
                                authPreferences.setMailSender(fromSender);
                            }
                        }
                        if (val.getName().equals("Subject")) {
                            subject = val.getValue();
                        }
                        if (val.getName().equals("To")) {
                            toReceiver = val.getValue();
                        }
                        if (val.getName().equals("Date")) {
                            dateReceived = val.getValue();
                            if (isFirstMailDate) {
                                isFirstMailDate = false;
                                authPreferences.setMailDate(dateReceived);
                            }
                        }
                    }
                    String messageSnippet = messageNew.getSnippet();
                    DatabaseInitializer.addGmailData(AppDatabase.getAppDatabase(this), fromSender, toReceiver, dateReceived,
                            builder.toString(), messageSnippet, msgID, subject, mailThreadID);
                    Log.i("TAG_GMAIL", "Mail is from: " + fromSender + " and Date is: " + dateReceived + " and subject is:" + subject);
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
        } else {
            Log.i("TAG", "Token not fetched.");
        }
    }


}
