package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getFragmentsPackage;

public final class FragmentMetadata extends BaseMetadata<TypeElement> {
    private final String layoutName;
    private final String screenName;
    private final String screenGroupName;
    private final List<Parameter> screenParameters;
    private final List<StatePersisted> persistedValues;
    private final List<ViewBinding> viewBindings;
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;

    public FragmentMetadata(TypeElement element, String layoutName, String screenName, String screenGroupName, List<Parameter> screenParameters, List<StatePersisted> persistedValues, List<ViewBinding> viewBindings) {
        super(element);
        this.layoutName = layoutName;
        this.screenName = screenName;
        this.screenGroupName = screenGroupName;
        this.screenParameters = screenParameters;
        this.persistedValues = persistedValues;
        this.viewBindings = viewBindings;
        this.className = ClassNames.getClassName(element);
        this.superClassName = ClassNames.getSuperClassName(element);
        this.instrumentedClassName = generateInstrumentedClassName(getFragmentsPackage(), className.simpleName());
    }

    public boolean isScreen() {
        return !(screenName == null || screenName.isEmpty());
    }

    public String getLayoutName() {
        return layoutName;
    }

    public String getScreenName() {
        return screenName;
    }

    public List<Parameter> getScreenParameters() {
        return screenParameters;
    }

    public List<StatePersisted> getPersistedValues() {
        return persistedValues;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getSuperClassName() {
        return superClassName;
    }

    public ClassName getInstrumentedClassName() {
        return instrumentedClassName;
    }

    public boolean hasViewBindings() {
        return !viewBindings.isEmpty();
    }

    public boolean hasPersistedValues() {
        return !persistedValues.isEmpty();
    }

    public boolean hasParameters() {
        return !screenParameters.isEmpty();
    }

    public boolean hasOptionalParameters() {
        return screenParameters.stream().anyMatch(Parameter::optional);
    }

    public List<Parameter> getScreenParameters(boolean includeOptional) {
        return includeOptional ? screenParameters : screenParameters.stream()
                .filter(paramInfo -> !paramInfo.optional())
                .collect(Collectors.toList());
    }

    public String getScreenGroupName() {
        if ((screenGroupName == null || screenGroupName.isEmpty())) {
            return "Common";
        }
        return screenGroupName;
    }

    public record Parameter(Element element, boolean optional) {
    }

    public record StatePersisted(Element element) {
    }

    public record ViewBinding(Element element, String name) {
    }
}