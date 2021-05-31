package com.example.chaterv3;

import android.app.Activity;
import android.widget.Toast;

public class Helpful {
    public static void toast(final Activity from, final String text){
        from.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(from.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
