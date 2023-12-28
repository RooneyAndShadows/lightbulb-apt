package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.util.SparseArray;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbParcelable;

import java.time.OffsetDateTime;
import java.util.*;

@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    public Date date;
    public OffsetDateTime offsetDateTime;
    public UUID uuid;
    public List<String> list;
    public SparseArray<String> arr;
    public UserDTO dto;
}
