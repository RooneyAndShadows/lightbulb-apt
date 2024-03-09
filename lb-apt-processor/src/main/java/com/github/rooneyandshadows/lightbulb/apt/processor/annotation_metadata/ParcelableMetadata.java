package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions.PARCEL;

public final class ParcelableMetadata extends ClassMetadata {
    private final List<TargetField> targetFields;
    private final List<IgnoredField> ignoredFields;
    private final boolean hasParcelConstructor;
    private final boolean superClassHasParcelConstructor;

    public ParcelableMetadata(TypeElement element, List<TargetField> targetFields, List<IgnoredField> ignoredFields) {
        super(element);
        this.targetFields = targetFields;
        this.ignoredFields = ignoredFields;
        this.hasParcelConstructor = checkForParcelConstructor();
        this.superClassHasParcelConstructor = checkSuperClassForParcelConstructor();
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
        TypeDefinition superClassTypeInfo = getTypeInformation().getSuperClassType();
        return superClassTypeInfo != null && superClassTypeInfo.hasConstructorWithParameters(ElementUtils::isAccessModifierAtLeastProtected, PARCEL.getCannonicalName());
    }

    private boolean checkForParcelConstructor() {
        TypeDefinition typeInfo = getTypeInformation();
        return typeInfo != null && typeInfo.hasConstructorWithParameters(PARCEL.getCannonicalName());
    }

    public static final class TargetField extends FieldMetadata {
        public TargetField(VariableElement element) {
            super(element);
        }
    }

    public static final class IgnoredField extends FieldMetadata {
        public IgnoredField(VariableElement element) {
            super(element);
        }
    }
}