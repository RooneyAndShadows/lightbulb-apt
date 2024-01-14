package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbFragment;


@LightbulbFragment(layoutName = "frag_main")
@FragmentScreen(screenName = "Login", screenGroup = "User")
public class Login extends Fragment {
    @FragmentParameter
    private int v;

    public void setV(int v) {
        this.v = v;
    }

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
