package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FragmentInfo {
    private final TypeMirror type;
    private final ClassName className;
    private final boolean canBeInstantiated;

    private ClassName bindingClassName;
    private String screenName;
    private FragmentConfiguration configAnnotation;

    private final List<FragmentViewBindingInfo> viewBindings = new ArrayList<>();
    private final List<FragmentParamInfo> fragmentParameters = new ArrayList<>();
    private final List<FragmentVariableInfo> fragmentPersistedVariables = new ArrayList<>();

    public FragmentInfo(Element fragmentClassElement, Elements elements) {
        type = fragmentClassElement.asType();
        className = ClassNames.generateFragmentClassName(fragmentClassElement, elements);
        canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
    }

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


    public void setBindingClassName(ClassName bindingClassName) {
        this.bindingClassName = bindingClassName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setConfigAnnotation(FragmentConfiguration configAnnotation) {
        this.configAnnotation = configAnnotation;
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getBindingClassName() {
        return bindingClassName;
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

    public List<FragmentViewBindingInfo> getViewBindings() {
        return viewBindings;
    }

    public List<FragmentParamInfo> getFragmentParameters() {
        return fragmentParameters;
    }

    public List<FragmentVariableInfo> getFragmentPersistedVariables() {
        return fragmentPersistedVariables;
    }
}