package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbStorage;

@LightbulbStorage(name = "global_storage")
public class GlobalStorage {
    public boolean enableStuff;
    public String themeId;
    public String locale;
}
