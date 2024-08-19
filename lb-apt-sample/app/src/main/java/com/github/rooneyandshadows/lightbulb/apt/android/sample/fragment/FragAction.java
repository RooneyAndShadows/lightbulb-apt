package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragActionBinding;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;


@LightbulbFragment(layoutName = "frag_action")
@FragmentScreen(screenName = "Action")
public class FragAction extends Fragment {
    @FragmentViewModel
    private FragActionVM viewModel;
    @FragmentViewBinding(layoutName = "frag_action")
    private FragActionBinding viewBinding;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding.testButton.setOnClickListener(v -> {
            LightbulbService.getFragmentResult().getFragHome().onListEdit(getParentFragmentManager(),1);
            LightbulbService.route().back();
        });
    }
}
