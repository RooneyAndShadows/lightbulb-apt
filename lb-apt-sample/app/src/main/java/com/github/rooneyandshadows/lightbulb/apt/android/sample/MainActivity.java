package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;

@LightbulbActivity()
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            LightbulbService.route().toCommonRoot(42).newRootScreen();
        }
    }
}