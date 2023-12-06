package com.github.rooneyandshadows.lightbulb.apt.processor.data.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_CLASS_NAME_PREFIX;

public abstract class BaseDescriptionBuilder<T extends BaseDescription> {
    protected final ClassName className;
    protected final ClassName superClassName;
    protected final ClassName instrumentedClassName;
    protected final boolean canBeInstantiated;

    public BaseDescriptionBuilder(Elements elements, TypeElement fragmentClassElement) {
        this.className = ClassNames.generateClassName(fragmentClassElement, elements);
        this.superClassName = ClassNames.generateSuperClassName(fragmentClassElement, elements);
        this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(PackageNames.getFragmentsPackage(), className.simpleName(), GENERATED_CLASS_NAME_PREFIX);
        this.canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
    }

    public abstract T build();
}
