package com.github.rooneyandshadows.lightbulb.apt.processor.data;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;

public final class LightbulbStorageDescription extends BaseDescription {
    private final String name;
    private final String[] subKeys;
    private final List<Field> fields;

    public LightbulbStorageDescription(ClassName className, ClassName superClassName, ClassName instrumentedClassName, boolean canBeInstantiated, String name, String[] subKeys, List<Field> fields) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.name = name;
        this.subKeys = subKeys;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public String[] getSubKeys() {
        return subKeys;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbStorageDescription> {
        private String name;
        private String[] subKeys;
        private List<Field> fields;

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getStoragePackage());
        }

        @Override
        public LightbulbStorageDescription build() {
            return new LightbulbStorageDescription(className, superClassName, instrumentedClassName, canBeInstantiated, name, subKeys, fields);
        }

        public void withName(String name) {
            this.name = name;
        }

        public void withSubKeys(String[] subKeys) {
            this.subKeys = subKeys;
        }

        public void withFields(List<Field> fields) {
            this.fields = fields;
        }
    }
}