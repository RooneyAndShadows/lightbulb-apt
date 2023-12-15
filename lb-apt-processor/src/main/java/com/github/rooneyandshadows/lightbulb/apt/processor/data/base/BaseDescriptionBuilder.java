package com.github.rooneyandshadows.lightbulb.apt.processor.data.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX;

public abstract class BaseDescriptionBuilder<T extends BaseDescription> {
    protected final ClassName className;
    protected final ClassName superClassName;
    protected final ClassName instrumentedClassName;
    protected final boolean canBeInstantiated;

    public BaseDescriptionBuilder(Elements elements, TypeElement fragmentClassElement, String instrumentedClassPackage) {
        this(elements, fragmentClassElement, instrumentedClassPackage, DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX);
    }

    public BaseDescriptionBuilder(Elements elements, TypeElement fragmentClassElement, String instrumentedClassPackage, String instrumentedClassNamePrefix) {
        this.className = ClassNames.generateClassName(fragmentClassElement, elements);
        this.superClassName = ClassNames.generateSuperClassName(fragmentClassElement, elements);
        this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(instrumentedClassPackage, className.simpleName(), instrumentedClassNamePrefix);
        this.canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
    }

    public abstract T build();
}
