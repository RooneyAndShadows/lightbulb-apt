package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.android.core.utils.BundleUtils;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.databinding.FragActionBinding;
import com.github.rooneyandshadows.lightbulb.apt.android.sample.lightbulb.service.LightbulbService;
import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import org.jetbrains.annotations.NotNull;


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
            Bundle data = new Bundle();
            BundleUtils.putParcelable("data", data, new ActionResult());
            getParentFragmentManager().setFragmentResult("DUMMY_KEY", data);
            LightbulbService.route().back();
            //FragmentResult.FragHome.onListUpdate(getParentFragmentManager(), "safas", 1, "asfasf", "asfasf");
            //Toast.makeText(requireContext(), String.valueOf(identifier), Toast.LENGTH_LONG).show();
        });
    }

    @LightbulbParcelable
    public static class ActionResult extends BlankParcelable {
        private final String message = "Wen moon? How's rocket? Where Lambo??";
        private final int id = 1;

        public String getMessage() {
            return message;
        }

        public int getId() {
            return id;
        }
    }
}
