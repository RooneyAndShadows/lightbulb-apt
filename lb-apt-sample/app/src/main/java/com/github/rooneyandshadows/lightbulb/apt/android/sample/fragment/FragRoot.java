package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragRootBinding;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;


@LightbulbFragment(layoutName = "frag_root")
@FragmentScreen(screenName = "Root")
public class FragRoot extends Fragment {
    @FragmentParameter
    private int identifier;
    @FragmentParameter
    private UUID userId;
    @FragmentStatePersisted
    private int persistedInt;
    @FragmentStatePersisted
    private String persistedString;
    @FragmentStatePersisted
    private Date persistedDate;
    @FragmentStatePersisted
    private UUID persistedUUID;
    @FragmentViewModel
    private FragRootVM viewModel;
    @FragmentViewBinding(layoutName = "frag_root")
    private FragRootBinding viewBinding;


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setIdentifier(identifier);
        viewBinding.testButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), String.valueOf(identifier), Toast.LENGTH_LONG).show();
        });
    }
}
