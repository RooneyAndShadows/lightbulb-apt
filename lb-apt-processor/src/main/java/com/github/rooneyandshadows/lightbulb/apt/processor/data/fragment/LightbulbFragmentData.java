package com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_CLASS_NAME_PREFIX;

public record LightbulbFragmentData(
        ClassName className,
        ClassName superClassName,
        ClassName instrumentedClassName,
        boolean canBeInstantiated,
        ScreenInfo screenInfo,
        Configuration configuration,
        List<Parameter> parameters,
        List<Variable> persistedVariables,
        List<ViewBinding> viewBindings
) {

    public List<Parameter> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? parameters : parameters.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public boolean hasOptionalParameters() {
        return parameters.stream().anyMatch(Parameter::isOptional);
    }

    public static final class Builder {
        private final ClassName className;
        private final ClassName superClassName;
        private final ClassName instrumentedClassName;
        private final boolean canBeInstantiated;
        private ScreenInfo screenInfo = null;
        private Configuration configuration = null;
        private final List<Parameter> parameters = new ArrayList<>();
        private final List<Variable> persistedVariables = new ArrayList<>();
        private final List<ViewBinding> viewBindings = new ArrayList<>();

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            //this.type = fragmentClassElement.asType();
            //this.superType = fragmentClassElement.getSuperclass();
            this.className = ClassNames.generateClassName(fragmentClassElement, elements);
            this.superClassName = ClassNames.generateSuperClassName(fragmentClassElement, elements);
            this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(PackageNames.getFragmentsPackage(), className.simpleName(), GENERATED_CLASS_NAME_PREFIX);
            this.canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
        }

        public LightbulbFragmentData build() {
            return new LightbulbFragmentData(
                    className,
                    superClassName,
                    instrumentedClassName,
                    canBeInstantiated,
                    screenInfo,
                    configuration,
                    parameters,
                    persistedVariables,
                    viewBindings
            );
        }

        public void withScreenInfo(ScreenInfo screenInfo) {
            this.screenInfo = screenInfo;
        }

        public void withConfiguration(Configuration configuration) {
            this.configuration = configuration;
        }

        public void withParameter(Parameter parameter) {
            this.parameters.add(parameter);
        }

        public void withPersistedVariable(Variable persistedVariable) {
            this.persistedVariables.add(persistedVariable);
        }

        public void withViewBinding(ViewBinding viewBinding) {
            this.viewBindings.add(viewBinding);
        }
    }
}