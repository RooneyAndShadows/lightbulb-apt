package com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
public class FragmentBindingData {
    private final TypeMirror type;
    private final ClassName className;
    private final ClassName bindingClassName;
    private final boolean canBeInstantiated;
    private ScreenInfo screenInfo;
    private Configuration configuration;
    private final List<Parameter> parameters = new ArrayList<>();
    private final List<Variable> persistedVariables = new ArrayList<>();
    private final List<ViewBinding> viewBindings = new ArrayList<>();
    private final String BINDING_CLASS_NAME_SUFFIX = "Bindings";

    public FragmentBindingData(Elements elements, Element fragmentClassElement, List<AnnotatedElement> annotatedElements) {
        this.type = fragmentClassElement.asType();
        this.className = ClassNames.generateClassName(fragmentClassElement, elements);
        this.bindingClassName = ClassNames.generateClassName(fragmentClassElement, elements, BINDING_CLASS_NAME_SUFFIX);
        this.canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
        annotatedElements.forEach(element -> {
            handleFragmentConfiguration(element);
            handleFragmentScreen(element);
            handleFragmentParameter(element);
            handleFragmentStatePersisted(element);
            handleFragmentBindView(element);
        });
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getBindingClassName() {
        return bindingClassName;
    }

    public boolean isCanBeInstantiated() {
        return canBeInstantiated;
    }

    public ScreenInfo getScreenInfo() {
        return screenInfo;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Variable> getPersistedVariables() {
        return persistedVariables;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }

    public boolean hasOptionalParameters() {
        for (Parameter param : parameters)
            if (param.isOptional()) return true;
        return false;
    }

    public List<Parameter> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? parameters : parameters.stream().filter(paramInfo -> !paramInfo.isOptional()).collect(Collectors.toList());
    }

    public String generateCommaSeparatedParams(boolean includeOptional, Consumer<Parameter> consumer) {
        String paramsString = "";
        List<Parameter> collection = getFragmentParameters(includeOptional);
        for (int index = 0; index < collection.size(); index++) {
            Parameter param = collection.get(index);
            boolean isLast = index == collection.size() - 1;
            consumer.accept(param);
            paramsString = paramsString.concat(param.getName());
            if (!isLast) paramsString = paramsString.concat(", ");
        }
        return paramsString;
    }

    private void handleFragmentConfiguration(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof FragmentConfiguration config)) return;
        configuration = new Configuration(
                config.isMainScreenFragment(),
                config.hasLeftDrawer(),
                config.hasOptionsMenu(),
                config.layoutName()
        );
    }

    private void handleFragmentScreen(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof FragmentScreen screen)) return;
        screenInfo = new ScreenInfo(screen.screenName(), screen.screenGroup());
    }

    private void handleFragmentParameter(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof FragmentParameter screen)) return;
        Parameter parameter = new Parameter(element.getElement(), screen.optional());
        parameters.add(parameter);
    }

    private void handleFragmentStatePersisted(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof FragmentStatePersisted statePersisted)) return;
        Variable variableInfo = new Variable(element.getElement());
        persistedVariables.add(variableInfo);

    }

    private void handleFragmentBindView(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof BindView bindView)) return;
        ViewBinding viewBindingInfo = new ViewBinding(element.getElement(), bindView.name());
        viewBindings.add(viewBindingInfo);
    }
}