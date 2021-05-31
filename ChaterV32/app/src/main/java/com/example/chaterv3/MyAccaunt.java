package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NavUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class MyAccaunt extends AppCompatActivity {
    public static ChatChoose caller;
    private Activity me = this;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Сохранить изменения (Ник)?").setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String, String> params = new HashMap<>();
                        try{
                            params.put("nickname", URLEncoder.encode(((EditText) findViewById(R.id.nickname)).getText().toString(), "UTF-8"));
                        }catch (Exception e){
                            Toast.makeText(me, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        HttpManager.callToApiNewThread("acc.updateinfo", params, new HttpManager.resultActionHolder() {
                            @Override
                            public void run(String result) {
                                Helpful.toast(me, "Успешно!");
                                LoginSystem.nickname = ((EditText) findViewById(R.id.nickname)).getText().toString();
                                finish();
                            }
                        }, me);
                    }
                }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_accaunt);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Ваш аккаунт");
        ab.setDisplayHomeAsUpEnabled(true);
        ((EditText) findViewById(R.id.nickname)).setText(LoginSystem.nickname);
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File loginData = new File(getFilesDir() + "login.data");
                if(loginData.exists()){
                    loginData.delete();
                    Intent intent = new Intent(MyAccaunt.this, Login.class);
                    startActivity(intent);
                    finishAffinity();
                    caller.finish();
                    finish();
                }
            }
        });


    }

}