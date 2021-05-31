package com.example.chaterv3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BackgroundWorker extends IntentService {

    public BackgroundWorker() {
        super("BackgroundWorker");
    }

    NotificationChannel channel;
    static int count = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT; // НЕ ПОМОГЛО
    }

    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("com.example.chaterv3.ANDROID", "ANDROID", NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), "com.example.chaterv3.ANDROID")
                    .setSmallIcon(R.drawable.othermsgbg)
                    .setAutoCancel(true)
                    .setContentTitle("СhatEr запущен")
                    .setContentText("Уведомления отсылаются").build();
            startForeground(101, notification); // НЕ ПОМОГЛО
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            Intent nextStepIntent = intent;
            HashMap<String, String> params = new HashMap<>();
            if(intent.hasExtra("token")){
                params.put("token", intent.getStringExtra("token"));
            }else{
                File loginData = new File(getFilesDir() + "login.data");
                if(loginData.exists()) {
                    try {
                        Scanner rider = new Scanner(loginData);
                        final String data = rider.next();
                        nextStepIntent.putExtra("token", data);
                    } catch (Exception e) {
                    }
                }
            }

            String result = HttpManager.callToApi("messages.getnew", params, false);
            if(result.toCharArray()[0] != 'E'){
                try {
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        Notification notification = new Notification.Builder(getApplicationContext(), "com.example.chaterv3.ANDROID")
                                .setSmallIcon(R.drawable.othermsgbg)
                                .setAutoCancel(true)
                                .setContentTitle(jsonobject.getString("sender"))
                                .setContentText(jsonobject.getString("text")).build();
                        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                .notify(101 + ++count, notification);
                    }
                }catch (Exception e){}




            }
            try {
                TimeUnit.SECONDS.sleep(5);
                this.startService(nextStepIntent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
