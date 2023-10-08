package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentBindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Variable;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.ViewBinding;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader.AnnotatedElement;
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
    private String screenName;
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
        setupFromAnnotations(annotatedElements);
    }

    public boolean hasOptionalParameters() {
        for (Parameter param : parameters) {
            if (param.isOptional()) {
                return true;
            }
        }
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

    public TypeMirror getType() {
        return type;
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


    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Variable> getPersistedVariables() {
        return persistedVariables;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }

    private void setupFromAnnotations(List<AnnotatedElement> annotatedElements) {
        annotatedElements.forEach(element -> {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof FragmentConfiguration config) {
                configuration = new Configuration(
                        config.isMainScreenFragment(),
                        config.hasLeftDrawer(),
                        config.hasOptionsMenu(),
                        config.layoutName()
                );
            } else if (annotation instanceof FragmentScreen screen) {
                screenName = screen.screenName();
            } else if (annotation instanceof com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentParameter param) {
                Parameter parameter = new Parameter(element.getElement(), param.optional());
                parameters.add(parameter);
            } else if (annotation instanceof FragmentStatePersisted) {

            } else if (annotation instanceof FragmentBindView) {

            }
        });
    }
}