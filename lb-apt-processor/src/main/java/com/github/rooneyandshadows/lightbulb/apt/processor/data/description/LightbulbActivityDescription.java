package com.github.rooneyandshadows.lightbulb.apt.processor.data.description;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public final class LightbulbActivityDescription extends BaseDescription {
    private final String fragmentContainerId;

    public LightbulbActivityDescription(
            ClassName className,
            ClassName superClassName,
            ClassName instrumentedClassName,
            boolean canBeInstantiated,
            String fragmentContainerId
    ) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.fragmentContainerId = fragmentContainerId;
    }

    public String getFragmentContainerId() {
        return fragmentContainerId;
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbActivityDescription> {
        private String fragmentContainerId;

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getActivitiesPackage());
        }

        @Override
        public LightbulbActivityDescription build() {
            return new LightbulbActivityDescription(
                    className,
                    superClassName,
                    instrumentedClassName,
                    canBeInstantiated,
                    fragmentContainerId
            );
        }

        public void withFragmentContainerId(String fragmentContainerId) {
            this.fragmentContainerId = fragmentContainerId;
        }
    }
}