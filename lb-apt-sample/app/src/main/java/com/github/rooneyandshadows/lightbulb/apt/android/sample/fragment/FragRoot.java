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
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentViewBinding;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentViewModel;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbFragment;
import org.jetbrains.annotations.NotNull;


@LightbulbFragment(layoutName = "frag_root")
@FragmentScreen(screenName = "Root")
public class FragRoot extends Fragment {
    @FragmentParameter
    private int identifier;
    @FragmentViewModel
    private FragRootVM viewModel;
    @FragmentViewBinding
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
