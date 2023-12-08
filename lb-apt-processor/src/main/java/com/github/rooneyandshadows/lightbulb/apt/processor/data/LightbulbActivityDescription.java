package com.github.rooneyandshadows.lightbulb.apt.processor.data;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.BaseDescriptionBuilder;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public final class LightbulbActivityDescription extends BaseDescription {
    private final boolean routingEnabled;
    private final String fragmentContainerId;

    public LightbulbActivityDescription(
            ClassName className,
            ClassName superClassName,
            ClassName instrumentedClassName,
            boolean canBeInstantiated,
            boolean routingEnabled,
            String fragmentContainerId
    ) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.routingEnabled = routingEnabled;
        this.fragmentContainerId = fragmentContainerId;
    }

    public boolean isRoutingEnabled() {
        return routingEnabled;
    }

    public String getFragmentContainerId() {
        return fragmentContainerId;
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbActivityDescription> {
        private boolean routingEnabled;
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
                    routingEnabled,
                    fragmentContainerId
            );
        }

        public void withRoutingEnabled(boolean routingEnabled) {
            this.routingEnabled = routingEnabled;
        }

        public void withFragmentContainerId(String fragmentContainerId) {
            this.fragmentContainerId = fragmentContainerId;
        }
    }
}