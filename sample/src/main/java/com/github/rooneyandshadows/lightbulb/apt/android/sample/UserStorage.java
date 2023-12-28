package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbStorage;

import java.util.List;

@LightbulbStorage(name = "global_storage", subKeys = {"userName"})
public class UserStorage {
    public String enableStuff;
    public List<String> cart;
}
