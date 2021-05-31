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
import java.io.FileWriter;
import java.util.HashMap;

public class Register extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Регистрация");

        Button signinButton = (Button) findViewById(R.id.signin);
        Button register = (Button) findViewById(R.id.go);
        final Register me = this;
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String log = ((EditText) findViewById(R.id.login)).getText().toString();
                String pass = ((EditText) findViewById(R.id.password)).getText().toString();
                String email = ((EditText) findViewById(R.id.email)).getText().toString();

                String error = null;

                if(log == null || log.length() <= 3){
                    error = getString(R.string.loginerror);
                }else if(pass == null || pass.length() < 8){
                    error = getString(R.string.passworderror);
                }else if(email == null || !LoginSystem.isValidEmail(email)){
                    error = getString(R.string.emailerror);
                }


                if(error != null){
                    Helpful.toast(me, error);
                }else{
                    LoginSystem.register(log, pass, email, me);
                }

                }

        });

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }




}