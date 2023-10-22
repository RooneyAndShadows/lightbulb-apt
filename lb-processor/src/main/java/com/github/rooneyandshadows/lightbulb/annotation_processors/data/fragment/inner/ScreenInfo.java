package com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner;

@SuppressWarnings("ClassCanBeRecord")
public class ScreenInfo {
    private final String screenGroupName;
    private final String screenName;

    public ScreenInfo(String screenGroupName, String screenName) {
        if (screenGroupName == null || screenGroupName.equals(""))
            screenGroupName = "Common";
        this.screenGroupName = screenGroupName;
        this.screenName = screenName;
    }

    public String getScreenGroupName() {
        return screenGroupName;
    }

    public String getScreenName() {
        return screenName;
    }
}