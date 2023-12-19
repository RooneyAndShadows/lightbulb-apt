package com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base;

import com.squareup.javapoet.ClassName;

public class BaseDescription {
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;
    private final boolean canBeInstantiated;

    public BaseDescription(ClassName className, ClassName superClassName, ClassName instrumentedClassName, boolean canBeInstantiated) {
        this.className = className;
        this.superClassName = superClassName;
        this.instrumentedClassName = instrumentedClassName;
        this.canBeInstantiated = canBeInstantiated;
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getSuperClassName() {
        return superClassName;
    }

    public ClassName getInstrumentedClassName() {
        return instrumentedClassName;
    }
}
