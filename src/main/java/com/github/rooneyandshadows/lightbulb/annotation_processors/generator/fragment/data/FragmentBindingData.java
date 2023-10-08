package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data;

import com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader.AnnotatedElement;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
public class FragmentBindingData {
    private final TypeMirror type;
    private final ClassName className;
    private final ClassName bindingClassName;
    private final String screenName;
    private final String layoutName;
    private final boolean canBeInstantiated;
    private final boolean isMainScreenFragment;
    private final boolean hasLeftDrawer;
    private final boolean hasOptionsMenu;
    private final List<Param> parameters = new ArrayList<>();
    private final List<Variable> persistedVariables = new ArrayList<>();
    private final List<ViewBinding> viewBindings = new ArrayList<>();
    private final String BINDING_CLASS_NAME_SUFFIX = "Bindings";

    public FragmentBindingData(Elements elements, Element fragmentClassElement, List<AnnotatedElement> annotations) {
        this.type = fragmentClassElement.asType();
        this.className = ClassNames.generateClassName(fragmentClassElement, elements);
        this.bindingClassName = ClassNames.generateClassName(fragmentClassElement, elements, BINDING_CLASS_NAME_SUFFIX);

    }

    public boolean hasOptionalParameters() {
        for (Param param : parameters) {
            if (param.isOptional()) {
                return true;
            }
        }
        return false;
    }

    public List<Param> getFragmentParameters(boolean includeOptional) {
        return includeOptional ? parameters : parameters.stream().filter(paramInfo -> !paramInfo.isOptional()).collect(Collectors.toList());
    }

    public String generateCommaSeparatedParams(boolean includeOptional, Consumer<Param> consumer) {
        String paramsString = "";
        List<Param> collection = getFragmentParameters(includeOptional);
        for (int index = 0; index < collection.size(); index++) {
            Param param = collection.get(index);
            boolean isLast = index == collection.size() - 1;
            consumer.accept(param);
            paramsString = paramsString.concat(param.name);
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

    public String getLayoutName() {
        return layoutName;
    }

    public boolean isCanBeInstantiated() {
        return canBeInstantiated;
    }

    public boolean isMainScreenFragment() {
        return isMainScreenFragment;
    }

    public boolean isHasLeftDrawer() {
        return hasLeftDrawer;
    }

    public boolean isHasOptionsMenu() {
        return hasOptionsMenu;
    }

    public List<Param> getParameters() {
        return parameters;
    }

    public List<Variable> getPersistedVariables() {
        return persistedVariables;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }
}