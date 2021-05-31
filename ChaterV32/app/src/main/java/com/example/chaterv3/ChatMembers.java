package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class UserContainer {
    private String username;
    private long userId = -1;
    private boolean isAdmin;

    public UserContainer(String username, long id, boolean isAdmin){
        this.username = username;
        userId = id;
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
    public long getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
}
class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserHolder>{

    private final LayoutInflater inflater;
    private final List<UserContainer> users;
    public RecyclerView parentView = null;
    private final Context context;
    private final Activity parentActivity;
    private final long chatId;
    UsersAdapter(Context context, List<UserContainer> users, Activity parent, long chatId) {
        parentActivity = parent;
        this.chatId = chatId;
        this.users = users;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public UsersAdapter.UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.user, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vieww) {
                final int itemPosition = parentView.getChildLayoutPosition(vieww);
                final UserContainer item = users.get(itemPosition);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final Thread modCaller = Thread.currentThread();
                builder.setTitle("Пользователь: " + item.getUsername());

                String[] admactions = {"Сделать админом", "Выгнать из чата"};
                if(item.isAdmin()){
                    admactions[0] = "Убрать админа";
                }
                builder.setItems(admactions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
                                    @Override
                                    public void run(String result) {
                                        Helpful.toast(parentActivity, "Успешно!");
                                        ((ChatMembers)parentActivity).update(chatId);
                                    }
                                };
                                HashMap<String, String> params = new HashMap<>();
                                params.put("user", String.valueOf(item.getUserId()));
                                params.put("chat", String.valueOf(chatId));
                                params.put("role", item.isAdmin() ? "default" : "admin");
                                HttpManager.callToApiNewThread("chat.setrole", params, holder, parentActivity);
                                break;
                            case 1:
                                holder = new HttpManager.resultActionHolder() {
                                    @Override
                                    public void run(String result) {
                                        Helpful.toast(parentActivity, "Успешно!");
                                        ((ChatMembers)parentActivity).update(chatId);
                                    }
                                };
                                params = new HashMap<>();
                                params.put("user", String.valueOf(item.getUserId()));
                                params.put("chat", String.valueOf(chatId));

                                HttpManager.callToApiNewThread("chat.kickuser", params, holder, parentActivity);
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        });
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(UsersAdapter.UserHolder holder, int position) {
        UserContainer state = users.get(position);

        holder.username.setText(state.getUsername());
        holder.avaText.setText(fullNameToInitials(state.getUsername()));
        if(state.isAdmin()){
            holder.username.setTextColor(ContextCompat.getColor(context, R.color.admincolor));
        }
    }
    private String fullNameToInitials(String name){
        String[] words = name.split(" ");
        StringBuilder out = new StringBuilder();
        for(String word : words){
            char[] c = word.toCharArray();
            if(Character.isLetter(c[0]) || Character.isDigit(c[0])){
                out.append(c[0]);
            }
        }
        String res = out.toString();
        return res == "" ? String.valueOf(name.toCharArray()[0]) : res;
    }
    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        final TextView username, avaText;
        UserHolder(View view){
            super(view);
            avaText  = (TextView) view.findViewById(R.id.avaText);
            username = (TextView) view.findViewById(R.id.username);
        }
    }

}
public class ChatMembers extends AppCompatActivity {
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
    final Activity me = this;
    public void update(final long chatId){

        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder(){
            @Override
            public void run(String result) {
                try {
                    JSONArray jsonarray = new JSONArray(result);
                    List<UserContainer> users = new ArrayList<UserContainer>();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        users.add( new UserContainer(jsonobject.getString("name"),jsonobject.getLong("id"), jsonobject.getBoolean("isAdmin")));
                    }

                    final List<UserContainer> usersFin = users;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UsersAdapter adapter = new UsersAdapter(me, usersFin, me, chatId);
                            adapter.parentView = me.findViewById(R.id.main);
                            ((RecyclerView)me.findViewById(R.id.main)).setAdapter(adapter);
                            ((RecyclerView)me.findViewById(R.id.main)).setLayoutManager(new LinearLayoutManager(me));
                        }
                    });

                }catch (final Exception e){
                    Helpful.toast(me, e.getMessage());
                }
            }
        };
        HashMap<String, String> params = new HashMap<>();
        params.put("chat", String.valueOf(chatId));
        HttpManager.callToApiNewThread("chat.getmembers", params, holder, me);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_members);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Участники чата");
        ab.setDisplayHomeAsUpEnabled(true);


        final long chatId = getIntent().getLongExtra("chatId", -1);
        if(chatId == -1){
            Toast.makeText(this, "Не задан id", Toast.LENGTH_SHORT).show();
            finishAffinity();
            return;
        }

        update(chatId);

    }
}