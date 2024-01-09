package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;

import javax.lang.model.element.TypeElement;

public final class ActivityMetadata extends ClassMetadata {
    private final String fragmentContainerId;

    public ActivityMetadata(TypeElement element, String fragmentContainerId) {
        super(element);
        this.fragmentContainerId = fragmentContainerId;
    }

    public String getFragmentContainerId() {
        return fragmentContainerId;
    }

}