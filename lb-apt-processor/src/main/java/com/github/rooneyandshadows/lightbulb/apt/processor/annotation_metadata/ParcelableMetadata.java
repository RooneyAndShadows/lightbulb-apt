package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.ANDROID_PARCEL_CANONICAL_NAME;

public final class ParcelableMetadata extends ClassMetadata {
    private final List<TargetField> targetFields;
    private final boolean hasParcelConstructor;

    public ParcelableMetadata(TypeElement element, List<TargetField> targetFields) {
        super(element);
        this.targetFields = targetFields;
        this.hasParcelConstructor = checkForParcelConstructor();
    }

    public List<TargetField> getTargetFields() {
        return targetFields;
    }

    public boolean hasParcelConstructor() {
        return hasParcelConstructor;
    }

    private boolean checkForParcelConstructor() {
        TypeInformation superClassTypeInfo = getTypeInformation().getSuperClassType();
        return superClassTypeInfo != null && superClassTypeInfo.hasConstructorWithParameters(ElementUtils::isAccessModifierAtLeastProtected, ANDROID_PARCEL_CANONICAL_NAME);
    }

    public static final class TargetField extends FieldMetadata {
        public TargetField(VariableElement element) {
            super(element);
        }
    }
}