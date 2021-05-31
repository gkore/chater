package com.example.chaterv3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {
    private final Activity me = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        File loginData = new File(getFilesDir() + "login.data");
        if(loginData.exists()){
            try {
                Scanner rider = new Scanner(loginData);
                final String data = rider.next();
                LoginSystem.userToken = data;
                HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
                    @Override
                    public void run(String result) {
                        LoginSystem.userToken = data;

                        Intent intent = new Intent(me, ChatChoose.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        me.startActivity(intent);
                        me.finishAffinity();
                    }
                };
                HttpManager.callToApiNewThread("acc.isvalidtoken", null, holder, this);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else{
            Intent intent = new Intent(MainActivity.this, Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            finishAffinity();
        }

        Intent intent = new Intent(me, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        me.startActivity(intent);
        me.finishAffinity();
    }
}