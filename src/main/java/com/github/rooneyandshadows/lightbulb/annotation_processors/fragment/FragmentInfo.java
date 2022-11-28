package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.squareup.javapoet.ClassName;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FragmentInfo {
    private TypeMirror type;
    private ClassName className;
    private ClassName mappedBindingType;
    private String screenName;
    private boolean canBeInstantiated;
    private FragmentConfiguration configAnnotation;
    private final Map<String, String> viewBindings = new HashMap<>();
    private final List<FragmentParamInfo> fragmentParameters = new ArrayList<>();
    private final List<FragmentVariableInfo> fragmentPersistedVariables = new ArrayList<>();

    public boolean hasOptionalParameters() {
        for (FragmentParamInfo param : getFragmentParameters())
            if (param.isOptional()) return true;
        return false;
    }

    public List<FragmentParamInfo> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? fragmentParameters : fragmentParameters.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public String generateCommaSeparatedParams(boolean includeOptional, Consumer<FragmentParamInfo> consumer) {
        String paramsString = "";
        List<FragmentParamInfo> collection = getFragmentParameters(includeOptional);
        for (int index = 0; index < collection.size(); index++) {
            FragmentParamInfo param = collection.get(index);
            boolean isLast = index == collection.size() - 1;
            consumer.accept(param);
            paramsString = paramsString.concat(param.name);
            if (!isLast)
                paramsString = paramsString.concat(", ");
        }
        return paramsString;
    }

    public void setType(TypeMirror type) {
        this.type = type;
    }

    public void setClassName(ClassName className) {
        this.className = className;
    }

    public void setMappedBindingType(ClassName mappedBindingType) {
        this.mappedBindingType = mappedBindingType;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setCanBeInstantiated(boolean canBeInstantiated) {
        this.canBeInstantiated = canBeInstantiated;
    }

    public void setConfigAnnotation(FragmentConfiguration configAnnotation) {
        this.configAnnotation = configAnnotation;
    }

    public TypeMirror getType() {
        return type;
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getMappedBindingType() {
        return mappedBindingType;
    }

    public String getScreenName() {
        return screenName;
    }

    public boolean isCanBeInstantiated() {
        return canBeInstantiated;
    }

    public FragmentConfiguration getConfigAnnotation() {
        return configAnnotation;
    }

    public Map<String, String> getViewBindings() {
        return viewBindings;
    }

    public List<FragmentParamInfo> getFragmentParameters() {
        return fragmentParameters;
    }

    public List<FragmentVariableInfo> getFragmentPersistedVariables() {
        return fragmentPersistedVariables;
    }
}