package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment;

import java.util.ArrayList;
import java.util.LinkedList;


@LightbulbFragment(layoutName = "frag_main")
@FragmentScreen(screenName = "Login", screenGroup = "User")
public class Login extends Fragment {
    // private MyView v;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //v = view.findViewById(R.id.ssssssssss);
        if (savedInstanceState == null) {
            //  v.init();
            // v.print();
        }

    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
