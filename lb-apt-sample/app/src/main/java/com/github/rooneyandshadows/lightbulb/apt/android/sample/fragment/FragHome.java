package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragRootBinding;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


@LightbulbFragment(layoutName = "frag_root")
@FragmentScreen(screenName = "Home")
public class FragHome extends Fragment {
    @FragmentParameter
    private int identifier;
    @FragmentParameter
    private UUID userId;
    @FragmentStatePersisted
    private int persistedInt;
    @FragmentViewModel
    private FragHomeVM viewModel;
    @FragmentViewBinding(layoutName = "frag_root")
    private FragRootBinding viewBinding;

    @ResultListener
    public void onListUpdate(String userName,UUID userId) {
        System.out.println("RESULT RECEIVED:");
        System.out.printf("userName: %s%n",userName);
        System.out.printf("userId: %s%n",userId.toString());
    }

    @ResultListener
    public void onListEdit(int num) {
        System.out.println("RESULT RECEIVED:");
        System.out.printf("userName: %s%n",num);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setIdentifier(identifier);
        viewBinding.testButton.setOnClickListener(v -> {
            LightbulbService.route().toCommonAction().forward();
        });
    }
}
