package com.github.rooneyandshadows.lightbulb.apt.android.sample;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbActivity;

@LightbulbActivity()
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
           // AppNavigator.route().toUserLogin("asfas", "safa", new Date()).newRootScreen();
          //  AppNavigator.route().printBackStack();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("rrrrrrrrrrrrr");
    }
}