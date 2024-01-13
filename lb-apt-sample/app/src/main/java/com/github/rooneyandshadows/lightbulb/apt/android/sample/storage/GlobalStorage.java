package com.github.rooneyandshadows.lightbulb.apt.android.sample.storage;


import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbStorage;

@LightbulbStorage(name = "global_storage")
public class GlobalStorage {
    public boolean enableStuff;
    public String themeId;
    public String locale;
}
