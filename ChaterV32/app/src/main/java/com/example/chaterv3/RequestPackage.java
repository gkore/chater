package com.example.chaterv3;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestPackage {
    private String url;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public RequestPackage(String url){
        this.url = url;
    }
    public RequestPackage(){}
}