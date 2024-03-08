package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.android.core.utils.BundleUtils;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragRootBinding;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
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
    @FragmentStatePersisted
    private String persistedString;
    @FragmentStatePersisted
    private Date persistedDate;
    @FragmentStatePersisted
    private UUID persistedUUID;
    @FragmentViewModel
    private FragHomeVM viewModel;
    @FragmentViewBinding(layoutName = "frag_root")
    private FragRootBinding viewBinding;

    //@ResultHandler(key = "DUMMY_KEY")
    //public void onListUpdate(FragAction.ActionResult result) {
    //    Toast.makeText(requireContext(), data, Toast.LENGTH_LONG).show();
    //}

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("DUMMY_KEY", this, (requestKey, result) -> {
            FragAction.ActionResult data = BundleUtils.getParcelable("data", result, FragAction.ActionResult.class);
            //onListUpdate(data);
        });
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setIdentifier(identifier);
        viewBinding.testButton.setOnClickListener(v -> {
            LightbulbService.route().toCommonAction().forward();
            //Toast.makeText(requireContext(), String.valueOf(identifier), Toast.LENGTH_LONG).show();
        });
    }

    @LightbulbParcelable
    public static class ActionResult extends BlankParcelable {
        private final String messages = "Test Data.";
        private final int id = 1;

        public String getMessages() {
            return messages;
        }

        public int getId() {
            return id;
        }
    }
}
