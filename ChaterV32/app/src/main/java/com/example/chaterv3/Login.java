package com.example.chaterv3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Вход в аккаунт");

        /*
        File loginData = new File(getFilesDir() + "login.data");
        if(loginData.exists()){
            try {
                Scanner rider = new Scanner(loginData);
                String data = rider.next();
                String[] splitted = data.split("####");
                String login = splitted[0];
                String pass  = splitted[1];
                LoginSystem.login(login, pass, this, false);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        */


        final Login me = this;
        Button regButton = (Button) findViewById(R.id.register);
        Button goButton = (Button) findViewById(R.id.go);
        regButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(me, Register.class);
                        startActivity(intent);
                        finishAffinity();
                    }catch (Exception e){
                        Helpful.toast(me, e.getMessage());
                    }
            }
        });
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean internet = LoginSystem.hasInternetConnection();
                if(!internet){
                    Toast.makeText(Login.this, getString(R.string.nointernet), Toast.LENGTH_LONG).show();
                    return;
                }else{
                    String log = ((EditText) findViewById(R.id.login)).getText().toString();
                    String pass = ((EditText) findViewById(R.id.password)).getText().toString();

                    String error = null;

                    if(log == null || log.length() <= 3){
                        error = getString(R.string.loginerror);
                    }else if(pass == null || pass.length() < 8){
                        error = getString(R.string.passworderror);
                    }

                    if(error != null){
                        Toast.makeText(me, error, Toast.LENGTH_SHORT).show();
                    }else{
                        LoginSystem.login(log, pass, me, true);
                    }

                }
            }
        });
    }

}