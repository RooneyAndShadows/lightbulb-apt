package com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public abstract class ElementDefinition<T extends Element> extends BaseDefinition<T> {
    protected final String name;
    protected final Modifier accessModifier;

    public ElementDefinition(T element) {
        super(element);
        this.name = element.getSimpleName().toString();
        this.accessModifier = ElementUtils.getAccessModifier(element);
    }

    public String getName() {
        return name;
    }

    public Modifier getAccessModifier() {
        return accessModifier;
    }
}
