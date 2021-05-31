package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatInfo extends AppCompatActivity {

    private boolean hasRedactedInfo = false;
    private final Activity me = this;
    private long chatId = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(hasRedactedInfo){

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Сохранить общие изменения (Название)?").setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("chat", String.valueOf(chatId));
                            params.put("name", ((EditText)me.findViewById(R.id.chatname)).getText().toString());
                            HttpManager.callToApiNewThread("chat.updateinfo", params, new HttpManager.resultActionHolder() {
                                @Override
                                public void run(String result) {
                                    Intent intent = new Intent(getApplicationContext(), ChatChoose.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }, me);
                        }
                    }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
                }else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View view = super.getView(position, convertView, parent);
            TextView tv = (TextView) view.findViewById(android.R.id.text1);
            if(position==2){
                tv.setTextColor(Color.RED);
            }else{
                tv.setTextColor(Color.DKGRAY);
            }
            return view;
        }
        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);
        Intent intent = getIntent();
        chatId = intent.getLongExtra("chatId", -1);
        if(chatId == -1){
            Toast.makeText(me, "Неизвестный номер чата", Toast.LENGTH_SHORT).show();
            finish();
        }

        final Object[] temp = new Object[2];
        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
            @Override
            public void run(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONArray jsonArray = new JSONArray(result);
                            String name = jsonArray.getJSONObject(0).getString("name");
                            boolean nots= jsonArray.getJSONObject(0).getBoolean("notifications");
                            ((EditText) findViewById(R.id.chatname)).setText(name);
                            ((Switch) findViewById(R.id.notificationsenable)).setChecked(nots);
                            temp[0] = name;
                            temp[1] = nots;

                            ((EditText) findViewById(R.id.chatname)).addTextChangedListener(new TextWatcher() {

                                @Override
                                public void afterTextChanged(Editable s) {
                                    //null
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s, int start,  int count, int after) {
                                    //null
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    if(s.length() == 0) return;
                                    hasRedactedInfo = true;
                                }
                            });
                        } catch (Exception e) {}
                    }
                });
            }
        };
        HashMap<String, String> params = new HashMap<>();
        params.put("chat", String.valueOf(chatId));
        HttpManager.callToApiNewThread("chat.myinfo", params, holder, me);//get info + put in boxes



        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Чат");

        final ListView listview = (ListView) findViewById(R.id.chatactions);
        String[] values = new String[] {"Просмотреть участников", "Пригласить людей", "Выйти из беседы"};

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                switch ((int) id) {
                    case 0:
                        Intent i = new Intent(me, ChatMembers.class);
                        i.putExtra("chatId", chatId);
                        startActivity(i);
                        break;
                    case 1:
                        HashMap<String, String> params = new HashMap<>();
                        params.put("chat", String.valueOf(chatId));

                        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
                            @Override
                            public void run(String result) {
                                final String generatedCode = result;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(me);
                                        builder1.setTitle(generatedCode);
                                        builder1.setMessage("Одноразовый код приглашения в беседу, прошлый код был удалён");
                                        builder1.setPositiveButton("Ок",null);
                                        Dialog dlg = builder1.create();

                                        Window window = dlg.getWindow();
                                        WindowManager.LayoutParams wlp = window.getAttributes();
                                        window.getAttributes().windowAnimations = R.style.DialogAnimation;

                                        wlp.gravity = Gravity.BOTTOM;
                                        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                                        window.setAttributes(wlp);

                                        dlg.show();
                                    }
                                });
                            }
                        };

                        HttpManager.callToApiNewThread("chat.newinvite", params, holder, me);

                        break;
                    case 2:
                        params = new HashMap<>();
                        params.put("chat", String.valueOf(chatId));
                        holder = new HttpManager.resultActionHolder() {
                            @Override
                            public void run(String result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(me, ChatChoose.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                            }
                        };

                        HttpManager.callToApiNewThread("chat.leave", params, holder, me);
                        break;
                }
            }

        });
    }
}


