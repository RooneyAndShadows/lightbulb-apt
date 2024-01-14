package com.github.rooneyandshadows.lightbulb.apt.android.sample.fragment;

import androidx.lifecycle.ViewModel;


public class FragRootVM extends ViewModel {
    private int identifier;

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}
