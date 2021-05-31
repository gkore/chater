package com.example.chaterv3;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class HttpManager {
    public final static String apiHost = "http://glebk.xyz/ChatEr-NoGui/";

    public interface resultActionHolder{
        void run(String result);
    }
    public static void callToApiNewThread(final String apiMethod, final HashMap<String, String> params, final resultActionHolder doAfter, final Activity caller){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!LoginSystem.hasInternetConnection()){
                        Helpful.toast(caller, "Нет подключения к сети");
                        return;
                    }

                    String buildedParams = "";
                    if(params != null) {
                        int i = 0;
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            buildedParams += (i == 0 ? "?" : "&") + entry.getKey() + "=" + entry.getValue();
                            i++;
                        }
                    }

                    URL url = new URL(apiHost + apiMethod + ".php" + buildedParams + (buildedParams=="" ? '?' : '&') + "token=" + LoginSystem.userToken);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    String result = "";
                    while ((inputLine = in.readLine()) != null) {
                        result += (inputLine);
                    }
                    in.close();


                    if(result.length() == 0){
                        return;
                    }
                    if(result.toCharArray()[0] == 'E'){
                        Helpful.toast(caller, result.substring(1));
                    }else if(doAfter != null){
                        doAfter.run(result);
                    }

                } catch (Exception e) {
                    Helpful.toast(caller, e.getMessage());
                }

            }
        }).start();
    }
    public static String callToApi(String apiMethod, HashMap<String, String> params, boolean addToken){
        if(!LoginSystem.hasInternetConnection()){
            return "EНет подключения к сети";
        }
        String buildedParams = "";
        if(params != null) {
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildedParams += (i == 0 ? "?" : "&") + entry.getKey() + "=" + entry.getValue();
                i++;
            }
        }
        int responseCode = -9999999;
        BufferedReader reader = null;
        try {
            URL url = new URL(apiHost + apiMethod + ".php" + buildedParams + (addToken ? ((buildedParams=="" ? '?' : '&') + "token=" + LoginSystem.userToken) : ""));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            responseCode = con.getResponseCode();
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            String result = "";
            while ((inputLine = in.readLine()) != null) {
                result += (inputLine);
            }
            in.close();

            return result;

        } catch (Exception e) {
            return responseCode + "CE1" + e.getClass().getName() ;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    return "CE2" + e.getMessage();
                }
            }
        }
    }
    @Deprecated
    public static String getData(String url){
        return getData(new RequestPackage(url));
    }
    @Deprecated
    public static String getData(RequestPackage requestPackage) {
        if(!LoginSystem.hasInternetConnection()){
            return "EНет подключения к сети";
        }
        BufferedReader reader = null;
        String uri = requestPackage.getUrl();
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line + '\n');
            }
            sb.substring(0, sb.length()-1);

            return sb.toString();

        } catch (Exception e) {
            return "CE" + e.getMessage();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    return "CE" + e.getMessage();
                }
            }
        }
    }
}
