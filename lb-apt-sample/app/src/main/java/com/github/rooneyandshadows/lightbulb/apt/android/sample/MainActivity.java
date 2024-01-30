package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;

import java.util.UUID;

@LightbulbActivity(layoutName = "activity_main", fragmentContainerId = "fragmentContainer")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            LightbulbService.route().toCommonRoot(0, UUID.randomUUID()).newRootScreen();
        }
    }
}