package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.element.Element;

public final class ActivityMetadata extends BaseMetadata {
    private final String fragmentContainerId;

    public ActivityMetadata(Element element, String fragmentContainerId) {
        super(element);
        this.fragmentContainerId = fragmentContainerId;
    }

    public String getFragmentContainerId() {
        return fragmentContainerId;
    }


}