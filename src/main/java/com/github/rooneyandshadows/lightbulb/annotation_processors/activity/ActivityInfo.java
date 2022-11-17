package com.github.rooneyandshadows.lightbulb.annotation_processors.activity;

import com.squareup.javapoet.ClassName;

public class ActivityInfo {
    private boolean routingEnabled;
    private ClassName className;

    public boolean isRoutingEnabled() {
        return routingEnabled;
    }

    public void setRoutingEnabled(boolean routingEnabled) {
        this.routingEnabled = routingEnabled;
    }

    public ClassName getClassName() {
        return className;
    }

    public void setClassName(ClassName className) {
        this.className = className;
    }
}