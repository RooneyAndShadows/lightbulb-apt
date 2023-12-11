package com.github.rooneyandshadows.lightbulb.apt.processor.data;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.base.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.ViewBinding;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class LightbulbFragmentDescription extends BaseDescription {
    private final boolean canBeInstantiated;
    private final String screenGroupName;
    private final String screenName;
    private final String layoutName;
    private final List<Parameter> parameters;
    private final List<Field> persistedVariables;
    private final List<ViewBinding> viewBindings;

    public LightbulbFragmentDescription(
            ClassName className,
            ClassName superClassName,
            ClassName instrumentedClassName,
            boolean canBeInstantiated,
            String screenGroupName,
            String screenName,
            String layoutName,
            List<Parameter> parameters,
            List<Field> persistedVariables,
            List<ViewBinding> viewBindings
    ) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.canBeInstantiated = canBeInstantiated;
        this.screenGroupName = (screenGroupName == null || screenGroupName.isEmpty()) ? "Common" : screenGroupName;
        this.screenName = screenName;
        this.layoutName = layoutName;
        this.parameters = parameters;
        this.persistedVariables = persistedVariables;
        this.viewBindings = viewBindings;
    }

    public boolean isScreen() {
        return !(screenName == null || screenName.isEmpty());
    }

    public boolean isCanBeInstantiated() {
        return canBeInstantiated;
    }

    public String getScreenGroupName() {
        return screenGroupName;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Field> getPersistedVariables() {
        return persistedVariables;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }

    public List<Parameter> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? parameters : parameters.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public boolean hasOptionalParameters() {
        return parameters.stream().anyMatch(Parameter::isOptional);
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbFragmentDescription> {
        private String screenName;
        private String screenGroupName;
        private String layoutName;
        private final List<Parameter> parameters = new ArrayList<>();
        private final List<Field> persistedVariables = new ArrayList<>();
        private final List<ViewBinding> viewBindings = new ArrayList<>();

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            super(elements, fragmentClassElement, PackageNames.getFragmentsPackage());
        }

        @Override
        public LightbulbFragmentDescription build() {
            return new LightbulbFragmentDescription(
                    className,
                    superClassName,
                    instrumentedClassName,
                    canBeInstantiated,
                    screenGroupName,
                    screenName,
                    layoutName,
                    parameters,
                    persistedVariables,
                    viewBindings
            );
        }

        public void withScreenName(String screenName) {
            this.screenName = screenName;
        }

        public void withScreenGroupName(String screenGroupName) {
            this.screenGroupName = screenGroupName;
        }

        public void withLayoutName(String layoutName) {
            this.layoutName = layoutName;
        }

        public void withParameter(Parameter parameter) {
            this.parameters.add(parameter);
        }

        public void withPersistedVariable(Field persistedVariable) {
            this.persistedVariables.add(persistedVariable);
        }

        public void withViewBinding(ViewBinding viewBinding) {
            this.viewBindings.add(viewBinding);
        }
    }
}