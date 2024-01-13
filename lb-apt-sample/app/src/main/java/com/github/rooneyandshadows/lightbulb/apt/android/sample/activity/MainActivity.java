package com.github.rooneyandshadows.lightbulb.apt.android.sample.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.R;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;


@LightbulbActivity()
public class MainActivity extends BaseActivity {

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        super.doOnCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            // AppNavigator.route().toUserLogin("asfas", "safa", new Date()).newRootScreen();
            //  AppNavigator.route().printBackStack();
        }
    }
}