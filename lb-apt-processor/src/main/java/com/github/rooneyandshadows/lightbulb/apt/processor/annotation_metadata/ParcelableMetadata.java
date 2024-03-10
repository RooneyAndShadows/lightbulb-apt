package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions.PARCEL;

public final class ParcelableMetadata extends ClassMetadata {
    private final boolean hasParcelConstructor;
    private final boolean superClassHasParcelConstructor;
    private final List<TargetField> targetFields = new ArrayList<>();
    private final List<IgnoredField> ignoredFields = new ArrayList<>();

    public ParcelableMetadata(ClassDefinition parcelableClassDefinition, List<AnnotatedElement> annotatedElements) {
        super(parcelableClassDefinition);
        this.hasParcelConstructor = checkForParcelConstructor();
        this.superClassHasParcelConstructor = checkSuperClassForParcelConstructor();
        extractValues(annotatedElements);
    }

    private void extractValues(List<AnnotatedElement> annotatedElements) {
        classDefinition.getFields().forEach(field -> {
            boolean isIgnored = field.hasAnnotation(IgnoreParcel.class);
            if (isIgnored) {
                ignoredFields.add(new IgnoredField(field));
            } else {
                targetFields.add(new TargetField(field));
            }
        });
    }

    public List<TargetField> getTargetFields() {
        return targetFields;
    }

    public List<IgnoredField> getIgnoredFields() {
        return ignoredFields;
    }

    public boolean hasParcelConstructor() {
        return hasParcelConstructor;
    }

    public boolean superClassHasParcelConstructor() {
        return superClassHasParcelConstructor;
    }

    private boolean checkSuperClassForParcelConstructor() {
        TypeDefinition superClassTypeInfo = getType().getSuperClassType();
        return superClassTypeInfo != null && superClassTypeInfo.hasConstructorWithParameters(ElementUtils::isAccessModifierAtLeastProtected, PARCEL.getCannonicalName());
    }

    private boolean checkForParcelConstructor() {
        TypeDefinition typeInfo = getType();
        return typeInfo != null && typeInfo.hasConstructorWithParameters(PARCEL.getCannonicalName());
    }

    public static final class TargetField extends FieldMetadata {
        public TargetField(FieldDefinition fieldDefinition) {
            super(fieldDefinition);
        }
    }

    public static final class IgnoredField extends FieldMetadata {
        public IgnoredField(FieldDefinition fieldDefinition) {
            super(fieldDefinition);
        }
    }
}