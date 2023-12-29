package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.generateInstrumentedClassName;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getActivitiesPackage;

public final class StorageMetadata extends BaseMetadata<TypeElement> {
    private final String name;
    private final String[] subKeys;
    private final List<TargetField> targetFields;
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;

    public StorageMetadata(TypeElement element, String name, String[] subKeys, List<TargetField> targetFields) {
        super(element);
        this.name = name;
        this.subKeys = subKeys;
        this.targetFields = targetFields;
        this.className = ClassNames.getClassName(element);
        this.superClassName = ClassNames.getSuperClassName(element);
        this.instrumentedClassName = generateInstrumentedClassName(getActivitiesPackage(), className.simpleName());
    }

    public String getName() {
        return name;
    }

    public String[] getSubKeys() {
        return subKeys;
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

    public List<TargetField> getTargetFields() {
        return targetFields;
    }

    public record TargetField(Element element) {
    }
}