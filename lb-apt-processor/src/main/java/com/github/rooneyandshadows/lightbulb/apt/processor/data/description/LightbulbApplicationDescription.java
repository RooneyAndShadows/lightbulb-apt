package com.github.rooneyandshadows.lightbulb.apt.processor.data.description;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public final class LightbulbApplicationDescription extends BaseDescription {

    public LightbulbApplicationDescription(
            ClassName className,
            ClassName superClassName,
            ClassName instrumentedClassName,
            boolean canBeInstantiated
    ) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
    }


    public static final class Builder extends BaseDescriptionBuilder<LightbulbApplicationDescription> {

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getApplicationPackage());
        }

        @Override
        public LightbulbApplicationDescription build() {
            return new LightbulbApplicationDescription(
                    className,
                    superClassName,
                    instrumentedClassName,
                    canBeInstantiated
            );
        }
    }
}