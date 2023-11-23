package com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_FRAGMENTS_CLASS_NAME_PREFIX;

@SuppressWarnings("FieldCanBeLocal")
public class FragmentBindingData {
    private final TypeMirror type;
    private final TypeMirror superType;
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;
    private final boolean canBeInstantiated;
    private ScreenInfo screenInfo;
    private Configuration configuration;
    private final List<Parameter> parameters = new ArrayList<>();
    private final List<ClassField> persistedVariables = new ArrayList<>();
    private final List<ViewBinding> viewBindings = new ArrayList<>();

    public FragmentBindingData(Elements elements, TypeElement fragmentClassElement, List<AnnotatedElement> annotatedElements) {
        this.type = fragmentClassElement.asType();
        this.superType = fragmentClassElement.getSuperclass();
        this.className = ClassNames.generateClassName(fragmentClassElement, elements);
        this.superClassName = ClassNames.generateSuperClassName(fragmentClassElement, elements);
        this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(PackageNames.getFragmentsPackage(), className.simpleName(), GENERATED_FRAGMENTS_CLASS_NAME_PREFIX);
        this.canBeInstantiated = ElementUtils.canBeInstantiated(fragmentClassElement);
        annotatedElements.forEach(element -> {
            handleFragmentConfiguration(element);
            handleFragmentScreen(element);
            handleFragmentParameter(element);
            handleFragmentStatePersisted(element);
            handleFragmentBindView(element);
        });
    }

    public String getSimpleClassName() {
        return className.simpleName();
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

    public List<ClassField> getPersistedVariables() {
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
        if (!(annotation instanceof LightbulbFragment config)) return;
        configuration = new Configuration(config.layoutName());
    }

    private void handleFragmentScreen(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof FragmentScreen screen)) return;
        screenInfo = new ScreenInfo(screen.screenGroup(), screen.screenName());
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
        ClassField variableInfo = new ClassField(element.getElement());
        persistedVariables.add(variableInfo);
    }

    private void handleFragmentBindView(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof BindView bindView)) return;
        ViewBinding viewBindingInfo = new ViewBinding(element.getElement(), bindView.name());
        viewBindings.add(viewBindingInfo);
    }
}