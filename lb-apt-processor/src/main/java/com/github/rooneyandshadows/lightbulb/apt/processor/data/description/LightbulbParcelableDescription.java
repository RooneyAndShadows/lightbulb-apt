package com.github.rooneyandshadows.lightbulb.apt.processor.data.description;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.STORAGE_CLASS_NAME_PREFIX;

public final class LightbulbParcelableDescription extends BaseDescription {
    private final List<Field> fields;

    public LightbulbParcelableDescription(ClassName className, ClassName superClassName, ClassName instrumentedClassName, boolean canBeInstantiated, List<Field> fields) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbParcelableDescription> {
        private List<Field> fields;

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getParcelablePackage());
        }

        @Override
        public LightbulbParcelableDescription build() {
            return new LightbulbParcelableDescription(className, superClassName, instrumentedClassName, canBeInstantiated, fields);
        }

        public void withFields(List<Field> fields) {
            this.fields = fields;
        }
    }
}