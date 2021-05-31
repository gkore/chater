package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.spec.ECField;
import java.util.HashMap;

public class CreateChat extends AppCompatActivity {
    public static ChatChoose parent;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Создание нового чата ");

        Button go = (Button) findViewById(R.id.create);
        final CreateChat me = this;
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String chatname = ((EditText) findViewById(R.id.chatname)).getText().toString();
                String error = null;
                char[] chars = chatname.toCharArray();

                if(chatname == null || chatname.length() <= 3){
                    error = getString(R.string.chatnameerror);
                }
                if(error != null){
                    Toast.makeText(me, error, Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("name", URLEncoder.encode(chatname, "UTF-8"));

                        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
                            @Override
                            public void run(final String result) {
                                me.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(me, "Успешно", Toast.LENGTH_SHORT).show();
                                        try {
                                            Thread.sleep(100);
                                            me.finish();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        };

                        HttpManager.callToApiNewThread("chat.create", params, holder, me);
                    }catch (Exception e){
                        Toast.makeText(me, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}