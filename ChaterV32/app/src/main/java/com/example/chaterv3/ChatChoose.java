package com.example.chaterv3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class ChatContainer {
    private String chatname;
    private long chatId = -1, newmsgcount = 0;

    public long getNewmsgcount() {
        return newmsgcount;
    }
    public boolean hasNewMsgs() {return newmsgcount != 0;}

    public ChatContainer(String chatname, long id, long newmsgcount){
        this.chatname = chatname;
        this.newmsgcount = newmsgcount;
        chatId = id;
    }

    public long getChatId() {
        return chatId;
    }
    public String getChatname() {
        return chatname;
    }
}
class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatHolder>{

    private final LayoutInflater inflater;
    private final List<ChatContainer> chats;
    public RecyclerView parentView = null;
    public Context toastContext;
    public ChatChoose caller;

    ChatsAdapter(Context context, List<ChatContainer> chats) {
        this.chats = chats;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public ChatsAdapter.ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.chat, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View vieww) {
                int itemPosition = parentView.getChildLayoutPosition(vieww);
                ChatContainer item = chats.get(itemPosition);
                caller.openChat(item);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View vieww) {
                try {
                    final int itemPosition = parentView.getChildLayoutPosition(vieww);
                    final ChatContainer item = chats.get(itemPosition);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(toastContext);
                    final Thread modCaller = Thread.currentThread();
                    builder.setTitle("Беседа: " + item.getChatname());

                    String[] actions = {"Пригласить людей", "Выйти из беседы"};
                    builder.setItems(actions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("chat", item.getChatId()+"");

                                    HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
                                        @Override
                                        public void run(String result) {
                                            final String generatedCode = result;
                                            caller.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(toastContext);
                                                    builder1.setTitle(generatedCode);
                                                    builder1.setMessage("Одноразовый код приглашения в беседу: "+ item.getChatname()+", прошлый код был удалён");
                                                    builder1.setPositiveButton("Ок",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {

                                                                }
                                                            });
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

                                    HttpManager.callToApiNewThread("chat.newinvite", params, holder, caller);

                                    break;
                                case 1:
                                    params = new HashMap<>();
                                    params.put("chat", ""+item.getChatId());

                                    holder = new HttpManager.resultActionHolder() {
                                        @Override
                                        public void run(String result) {
                                            caller.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                chats.remove(itemPosition);
                                                notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    };

                                    HttpManager.callToApiNewThread("chat.leave", params, holder, caller);
                                    break;
                            }
                        }
                    });

                    Dialog dlg = builder.create();

                    Window window = dlg.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    window.getAttributes().windowAnimations = R.style.DialogAnimation;

                    wlp.gravity = Gravity.BOTTOM;
                    wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    window.setAttributes(wlp);

                    dlg.show();
                }catch (Exception e){
                    Toast.makeText(toastContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        return new ChatHolder(view);
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
    public void onBindViewHolder(ChatsAdapter.ChatHolder holder, int position) {
        ChatContainer state = chats.get(position);

        holder.chatname.setText(state.getChatname());
        if(state.hasNewMsgs()){
            holder.newcount.setText(String.valueOf(state.getNewmsgcount()));
            holder.newcount.setVisibility(View.GONE);
        }else{
            holder.newcount.setVisibility(View.GONE);
        }
        holder.avaText.setText(fullNameToInitials(state.getChatname()));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {
        final TextView chatname, newcount, avaText;
        ChatHolder(View view){
            super(view);
            avaText  = (TextView) view.findViewById(R.id.avaText);
            chatname = (TextView) view.findViewById(R.id.chatname);
            newcount = (TextView) view.findViewById(R.id.newmsgcount);
        }
    }

}
public class ChatChoose extends AppCompatActivity {
    RecyclerView recView;
    final ArrayList<ChatContainer> chats = new ArrayList<ChatContainer>();

    public int dpToPx(int dp) {
        float density = getApplicationContext().getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
    public void updateDisplay(int itemToScroll) { // -1 == adapter.getItemCount()-1
        if (itemToScroll == -1) {
            itemToScroll = chats.size() - 1;
        }

        ChatsAdapter adapter = new ChatsAdapter(this, chats);
        adapter.parentView = recView;
        adapter.toastContext = ChatChoose.this;
        adapter.caller = this;
        recView.setAdapter(adapter);

        if(itemToScroll == -666)return;

        recView.scrollToPosition(itemToScroll);
    }
    public void openChat(ChatContainer chatToOpen){
        chat.setChat(chatToOpen);
        Intent intent = new Intent(ChatChoose.this, chat.class);
        startActivity(intent);
    }
    final ChatChoose me = this;
    public void getInfo(){
        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
            @Override
            public void run(final String result) {
                try {
                    JSONArray jsonarray = new JSONArray(result);


                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        chats.add(0, new ChatContainer(jsonobject.getString("name"), jsonobject.getLong("id"), jsonobject.getLong("newmsgscount")));
                    }
                    if (chats.size() > 0) {
                        me.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay(0);
                            }
                        });
                    }
                }catch (final Exception e){
                    Helpful.toast(me, e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                });
            }
        };
        HttpManager.callToApiNewThread("chat.getmy", new HashMap<String, String>(), holder, me);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newchat:
                CreateChat.parent = this;
                Intent intent = new Intent(ChatChoose.this, CreateChat.class);
                startActivity(intent);
                return true;
            case R.id.joinchat:
                JoinChat.lastCaller = this;
                intent = new Intent(ChatChoose.this, JoinChat.class);
                startActivity(intent);
                return true;
            case R.id.myacc:
                MyAccaunt.caller = this;
                intent = new Intent(ChatChoose.this, MyAccaunt.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_choose);
        if(getIntent().hasExtra("token")){
            LoginSystem.userToken = getIntent().getStringExtra("token"); //если мы по уведомлению
        }

        recView = findViewById(R.id.main);
        recView.setLayoutManager(new LinearLayoutManager(this));

        final SwipeRefreshLayout root = findViewById(R.id.root);
        root.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chats.clear();
                getInfo();
                root.setRefreshing(false);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new Thread(new Runnable() {// НЕ ПОМОГЛО
                @Override
                public void run() {
                    startForegroundService(new Intent(me, BackgroundWorker.class).putExtra("token", LoginSystem.userToken));// НЕ ПОМОГЛО
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chats.clear();
        getInfo();
    }
}