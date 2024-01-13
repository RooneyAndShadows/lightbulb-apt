package com.github.rooneyandshadows.lightbulb.apt.android.sample.storage;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbStorage;

import java.util.List;

@LightbulbStorage(name = "global_storage", subKeys = {"userName"})
public class UserStorage {
    public String enableStuff;
    public List<String> cart;
}
