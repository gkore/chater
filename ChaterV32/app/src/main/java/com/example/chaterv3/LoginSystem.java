package com.example.chaterv3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;


public class LoginSystem extends AppCompatActivity {
    public static String userToken = null, nickname = "";

    public static boolean hasInternetConnection() {
        try {
            InetAddress address = InetAddress.getByAddress(new byte[]{8,8,8,8});
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void login(final String login, final String password, final Activity caller, final boolean needRemember){
        HashMap<String, String> params = new HashMap<>();
        params.put("log", login);
        params.put("pass", password);
        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
            @Override
            public void run(final String result) {
                caller.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String rawdata = result;
                        String[] data = rawdata.split("#");
                        LoginSystem.userToken = data[0];
                        LoginSystem.nickname  = data[1];

                        if(needRemember) {
                            try {
                                File loginData = new File(caller.getFilesDir() + "login.data");
                                if (!loginData.exists()) {
                                    loginData.createNewFile();
                                }
                                FileWriter myWriter = new FileWriter(caller.getFilesDir() + "login.data");
                                myWriter.write(data[0]);

                                myWriter.close();
                            } catch (Exception e) {
                                Helpful.toast(caller, e.getMessage());
                            }
                        }

                        Intent intent = new Intent(caller, ChatChoose.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        caller.startActivity(intent);
                        caller.finishAffinity();
                    }
                });
            }
        };

        HttpManager.callToApiNewThread("acc.login", params, holder, caller);
    }

    public static void register(final String log, final String pass, final String email, final Activity me){
        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
            @Override
            public void run(String result) {
                me.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            File loginData = new File(me.getFilesDir() + "login.data");
                            if(!loginData.exists()) {
                                loginData.createNewFile();
                            }

                            Intent intent = new Intent(me, Login.class);
                            me.startActivity(intent);
                            me.finishAffinity();
                        }catch (Exception e){
                            Helpful.toast(me, e.getMessage());
                        }
                    }
                });
            }
        };
        HashMap<String, String> params = new HashMap<>();
        params.put("log"  , log);
        params.put("pass" , pass);
        params.put("email", email);
        HttpManager.callToApiNewThread("acc.register", params, holder, me);
    }

}
