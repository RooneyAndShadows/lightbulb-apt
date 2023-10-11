package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner;

public class Configuration {
    private boolean isMainScreenFragment;
    private boolean hasLeftDrawer;
    private boolean hasOptionsMenu;
    private String layoutName;

    public Configuration(boolean isMainScreenFragment, boolean hasLeftDrawer, boolean hasOptionsMenu, String layoutName) {
        this.isMainScreenFragment = isMainScreenFragment;
        this.hasLeftDrawer = hasLeftDrawer;
        this.hasOptionsMenu = hasOptionsMenu;
        this.layoutName = layoutName;
    }

    public boolean isMainScreenFragment() {
        return isMainScreenFragment;
    }

    public boolean isHasLeftDrawer() {
        return hasLeftDrawer;
    }

    public boolean isHasOptionsMenu() {
        return hasOptionsMenu;
    }

    public String getLayoutName() {
        return layoutName;
    }
}
