package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragActionBinding;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


@LightbulbFragment(layoutName = "frag_action")
@FragmentScreen(screenName = "Action")
public class FragAction extends Fragment {
    @FragmentViewModel
    private FragActionVM viewModel;
    @FragmentViewBinding(layoutName = "frag_action")
    private FragActionBinding viewBinding;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding.testButton.setOnClickListener(v -> {
            LightbulbService.getFragmentResult().getFragHome().onListUpdate(getParentFragmentManager(),"userName1", UUID.randomUUID());
            LightbulbService.route().back();
        });
    }
}
