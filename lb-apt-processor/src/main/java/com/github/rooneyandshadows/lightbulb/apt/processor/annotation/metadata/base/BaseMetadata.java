package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base;

import javax.lang.model.element.Element;

public class BaseMetadata {
    protected Element element;

    public BaseMetadata(Element element) {
        this.element = element;
    }
}
