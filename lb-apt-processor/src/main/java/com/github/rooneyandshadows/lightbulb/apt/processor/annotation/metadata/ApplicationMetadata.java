package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.generateInstrumentedClassName;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getActivitiesPackage;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getApplicationPackage;

public final class ApplicationMetadata extends BaseMetadata<TypeElement> {
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;

    public ApplicationMetadata(TypeElement element) {
        super(element);
        this.className = ClassNames.getClassName(element);
        this.superClassName = ClassNames.getSuperClassName(element);
        this.instrumentedClassName = generateInstrumentedClassName(getApplicationPackage(), className.simpleName());
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