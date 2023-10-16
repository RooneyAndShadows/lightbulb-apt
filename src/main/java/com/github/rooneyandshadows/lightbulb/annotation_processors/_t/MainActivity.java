package com.github.rooneyandshadows.lightbulb.annotation_processors._t;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String binding = Bindings.getBindingForClass(this.getClass());



    }
}