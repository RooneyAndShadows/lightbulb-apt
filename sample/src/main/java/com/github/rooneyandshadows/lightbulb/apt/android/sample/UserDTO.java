package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbParcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    public ArrayList<String> cart;
}
