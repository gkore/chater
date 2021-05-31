package com.example.chaterv3;

import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

public class MessageUtils {

    private long maxDeleteId = Long.MAX_VALUE, maxEditId = Long.MAX_VALUE, maxId = Long.MAX_VALUE;

    private String parseMessageText(String rawText){
        if(rawText == null || rawText.length() == 0) return "";
        char[] text = rawText.toCharArray();
        char lastSymb = text[0];
        StringBuilder result = new StringBuilder();
        result.append(lastSymb);
        for (int i = 1; i < text.length; i++) {
            if(!(lastSymb == ' ' && text[i] == ' ')){
                result.append(text[i]);
            }
            lastSymb = text[i];
        }
        return result.toString().replace(" ", "").length() != 0 ? result.toString() : "";
    }
    public void editMsg(final chat me, final long msgId, final String rawText, final Message reply) {
        try {
            long chatId = chat.chatId;
            final String text = parseMessageText(rawText);
            if(text == ""){
                Toast.makeText(me, "Пустое сообщение - плохо :P", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> params = new HashMap<>();
            try {
                params.put("text", URLEncoder.encode(text, "UTF-8"));
            }catch (Exception e){}
            params.put("chat", String.valueOf(chatId));
            params.put("msgId", String.valueOf(msgId));
            if(reply != null) {
                params.put("reply", String.valueOf(reply.getId()));
            }
            HttpManager.callToApiNewThread("message.edit", params, new HttpManager.resultActionHolder() {
                @Override
                public void run(final String result) {
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int i = 0;
                                for (Message message : me.messages) {
                                    if(message.getId() == msgId){
                                        me.messages.get(i).setText(rawText);
                                        me.messages.get(i).setEdited(true);
                                        if(reply != null){
                                            me.messages.get(i).setReply(reply);
                                        }
                                    }
                                    i++;
                                }
                                me.recView.getAdapter().notifyDataSetChanged();
                            } catch (Exception e) {
                                Helpful.toast(me, e.getMessage());
                            }
                        }
                    });
                }
            }, me);
        }catch (Exception e){
            Toast.makeText(me, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMsg(final chat me, final String rawText, final Message reply) {

        try {
            long chatId = chat.chatId;
            final String text = parseMessageText(rawText);
            if(text == ""){
                Toast.makeText(me, "Нельзя просто взять и отправить пустое сообщение :P", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> params = new HashMap<>();
            try {
                params.put("text", URLEncoder.encode(text, "UTF-8"));
            }catch (Exception e){}
            params.put("chat", String.valueOf(chatId));
            if(reply != null) {
                params.put("reply", String.valueOf(reply.getId()));
            }
            HttpManager.callToApiNewThread("message.send", params, new HttpManager.resultActionHolder() {
                @Override
                public void run(final String result) {
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Long id = Long.valueOf(result);
                                maxId = id;
                                me.messages.add(new Message(id, text, true, System.currentTimeMillis()/1000, "Вы", false, reply));
                                me.updateDisplay(-1);
                            } catch (Exception e) {
                                Helpful.toast(me, e.getMessage());
                            }
                        }
                    });
                }
            }, me);
        }catch (Exception e){
            Toast.makeText(me, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void jsonArrayToMsgs(JSONArray jsonarray, final chat me) throws JSONException {
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            Long id = jsonobject.getLong("id");
            if(id > maxId){
                maxId = id; //НА ВСЯКИЙ СЛУЧАЙ
            }
            me.messages.add(0, new Message(id,
                    jsonobject.getString("text"),
                    jsonobject.getBoolean("isMine"),
                    Long.valueOf(jsonobject.getString("sendtime")),
                    jsonobject.getString("sender"),
                    jsonobject.getBoolean("edited"),
                    jsonobject.has("replyId") ?
                            new Message(jsonobject.getLong("replyId"),jsonobject.getString("replyText"),false,0, jsonobject.getString("replySender"),false,null)
                            :
                            null
                    ));
        }
        if(jsonarray.length() == 0) return;
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                me.updateDisplay(-1);
            }
        });
    }
    private void jsonArrayToMsgsTop(final JSONArray jsonarray, final chat me) throws JSONException {
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);

            me.messages.add(0, new Message(jsonobject.getLong("id"),
                    jsonobject.getString("text"),
                    jsonobject.getBoolean("isMine"),
                    Long.valueOf(jsonobject.getString("sendtime")),
                    jsonobject.getString("sender"),
                    jsonobject.getBoolean("edited"),
                    jsonobject.has("replyId") ?
                    new Message(jsonobject.getLong("replyId"),jsonobject.getString("replyText"),false,0, jsonobject.getString("replySender"),false,null)
                    :
                    null
                    ));
        }
        me.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (jsonarray.length() == 0) {
                    me.recView.scrollBy(0, 1);
                } else {
                    me.updateDisplay(jsonarray.length());
                }
            }
        });
    }

    public void getLast(final chat me) {
        HashMap<String, String> params = new HashMap<>();
        params.put("chat", "" + me.chatId);
        params.put("from", "" + me.loadedMsgs);
        params.put("count", "" + me.msgLoadCount);
        params.put("read", "da");

        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder(){
            @Override
            public void run(String result) {
                try {
                    me.loadedMsgs += me.msgLoadCount;
                    jsonArrayToMsgs(new JSONArray(result), me);
                }catch (final Exception e){
                    Helpful.toast(me, e.getMessage());
                }
            }
        };

        HttpManager.callToApiNewThread("message.getpart", params, holder, me);
    }

    public void getPart(final chat me) {
        HashMap<String, String> params = new HashMap<>();
        params.put("chat", "" + me.chatId);
        params.put("from", "" + me.loadedMsgs);
        params.put("count", "" + me.msgLoadCount);

        HttpManager.resultActionHolder holder = new HttpManager.resultActionHolder() {
            @Override
            public void run(String result) {
                try {
                    me.loadedMsgs += me.msgLoadCount;
                    jsonArrayToMsgsTop(new JSONArray(result),me);
                }catch (final Exception e){
                    Helpful.toast(me, e.getMessage());
                }
            }
        };
        HttpManager.callToApiNewThread("message.getpart", params, holder, me);
    }

    public void getNews(final chat me) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("chat",        String.valueOf(me.chatId));
        params.put("maxDeleteId", String.valueOf(maxDeleteId));
        params.put("maxEditId",   String.valueOf(maxEditId));
        params.put("maxId",       String.valueOf(maxId));
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = HttpManager.callToApi("message.whatsnew", params, true);
                if(result.toCharArray()[0] == 'E') {
                    Helpful.toast(me, result.substring(1));
                    return;
                }
                me.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //((EditText)me.findViewById(R.id.message)).setText(result);
                            JSONArray jsonarray = new JSONArray(result);

                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String type = jsonobject.getString("type");
                                switch (type){
                                    case "newmsg":
                                        me.messages.add(new Message(jsonobject.getLong("id"),
                                            jsonobject.getString("text"),
                                            jsonobject.getBoolean("isMine"),
                                            Long.valueOf(jsonobject.getString("sendtime")),
                                            jsonobject.getString("sender"),
                                            false,
                                            jsonobject.has("replyId") ?
                                            new Message(jsonobject.getLong("replyId"),jsonobject.getString("replyText"),false,0, jsonobject.getString("replySender"),false,null)
                                            :
                                            null
                                            ));

                                        me.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                me.updateDisplay(-1);
                                            }
                                        });
                                        break;
                                    case "deletemsg":
                                        int j = 0;
                                        for (Message message : me.messages) {
                                            if(message.getId() == jsonobject.getLong("id")){
                                                me.messages.remove(message);
                                            }
                                            else if(message.getReply() != null && message.getReply().getId() == jsonobject.getLong(("id"))){
                                                me.messages.get(j).deleteReply();
                                            }
                                            j++;
                                        }
                                        me.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                me.updateDisplayNoScroll();
                                            }
                                        });
                                        break;
                                    case "editmsg":
                                        i = 0;
                                        for (Message message : me.messages) {
                                            if(message.getId() == jsonobject.getLong("id")){
                                                me.messages.get(i).setText(jsonobject.getString("text"));
                                                me.messages.get(i).setEdited(true);
                                            }
                                            i++;
                                        }
                                        me.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                me.updateDisplayNoScroll();
                                            }
                                        });
                                        break;
                                    case "maxDeleteId":
                                        maxDeleteId = jsonobject.getLong("deleteId");
                                        break;
                                    case "maxEditId":
                                        maxEditId = jsonobject.getLong("editId");
                                        break;
                                    case "maxId":
                                        maxId = jsonobject.getLong("id");
                                        //Helpful.toast(me, String.valueOf(maxId));
                                        break;
                                }
                            }

                        }catch (final Exception e){
                            Helpful.toast(me, e.getMessage());
                        }
                    }
                });
            }
        }).start();
    }

}
