package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class JoinChat extends AppCompatActivity {
    public static ChatChoose lastCaller;
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
        setContentView(R.layout.activity_join_chat);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Вход в чат по коду");

        final JoinChat me = this;
        findViewById(R.id.join).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> params = new HashMap<String,String>();
                        params.put("code", ((EditText)findViewById(R.id.code)).getText().toString());
                        final String result = HttpManager.callToApi("chat.join", params, true);
                        if(result.toCharArray()[0] == 'E'){
                            me.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(me, result.substring(1), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            String[] data = result.substring(4).split(":");
                            lastCaller.chats.add(new ChatContainer(data[0], Long.valueOf(data[1]), 0));
                            me.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(me, "Удачно!", Toast.LENGTH_SHORT).show();
                                    lastCaller.recView.getAdapter().notifyDataSetChanged();
                                }
                            });
                            me.finish();
                        }
                    }
                }).start();
                    }
                }
        );
    }
}