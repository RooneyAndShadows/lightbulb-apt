package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentInfo {
    private TypeMirror type;
    private ClassName className;
    private ClassName mappedBindingType;
    private String screenName;
    private boolean canBeInstantiated;
    private FragmentConfiguration configAnnotation;
    private final Map<String, String> viewBindings = new HashMap<>();
    private final List<FragmentParamInfo> fragmentParameters = new ArrayList<>();

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
}