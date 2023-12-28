package com.github.rooneyandshadows.lightbulb.apt.processor.data.description;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.base.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldPersisted;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldViewBinding;
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
    private final List<FieldScreenParameter> screenParameterFields;
    private final List<FieldPersisted> persistedFields;
    private final List<FieldViewBinding> viewBindingFields;

    public LightbulbFragmentDescription(
            ClassName className,
            ClassName superClassName,
            ClassName instrumentedClassName,
            boolean canBeInstantiated,
            String screenGroupName,
            String screenName,
            String layoutName,
            List<FieldScreenParameter> screenParameterFields,
            List<FieldPersisted> persistedFields,
            List<FieldViewBinding> viewBindingFields
    ) {
        super(className, superClassName, instrumentedClassName, canBeInstantiated);
        this.canBeInstantiated = canBeInstantiated;
        this.screenGroupName = (screenGroupName == null || screenGroupName.isEmpty()) ? "Common" : screenGroupName;
        this.screenName = screenName;
        this.layoutName = layoutName;
        this.screenParameterFields = screenParameterFields;
        this.persistedFields = persistedFields;
        this.viewBindingFields = viewBindingFields;
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

    public List<FieldScreenParameter> getScreenParameterFields() {
        return screenParameterFields;
    }

    public List<FieldPersisted> getPersistedFields() {
        return persistedFields;
    }

    public List<FieldViewBinding> getViewBindingFields() {
        return viewBindingFields;
    }

    public List<FieldScreenParameter> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? screenParameterFields : screenParameterFields.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public boolean hasOptionalParameters() {
        return screenParameterFields.stream().anyMatch(FieldScreenParameter::isOptional);
    }

    public static final class Builder extends BaseDescriptionBuilder<LightbulbFragmentDescription> {
        private String screenName;
        private String screenGroupName;
        private String layoutName;
        private final List<FieldScreenParameter> parameters = new ArrayList<>();
        private final List<FieldPersisted> persistedVariables = new ArrayList<>();
        private final List<FieldViewBinding> viewBindings = new ArrayList<>();

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

        public void withParameter(FieldScreenParameter parameter) {
            this.parameters.add(parameter);
        }

        public void withPersistedVariable(FieldPersisted persistedVariable) {
            this.persistedVariables.add(persistedVariable);
        }

        public void withViewBinding(FieldViewBinding viewBinding) {
            this.viewBindings.add(viewBinding);
        }
    }
}