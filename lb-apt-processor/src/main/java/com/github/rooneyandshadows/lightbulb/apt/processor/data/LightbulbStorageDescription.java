package com.github.rooneyandshadows.lightbulb.apt.processor.data;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public final class LightbulbStorageDescription extends BaseDescription {
    private final String name;

    public LightbulbStorageDescription(ClassName className, ClassName superClassName, ClassName instrumentedClassName, boolean canBeInstantiated, String name) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbStorageDescription> {
        private String name;

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getStoragePackage());
        }

        @Override
        public LightbulbStorageDescription build() {
            return new LightbulbStorageDescription(className, superClassName, instrumentedClassName, canBeInstantiated, name);
        }

        public void withName(String name) {
            this.name = name;
        }
    }
}