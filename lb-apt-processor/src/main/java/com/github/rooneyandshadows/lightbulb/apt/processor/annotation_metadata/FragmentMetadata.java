package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FragmentMetadata extends ClassMetadata {
    private String layoutName;
    private String screenName;
    private String screenGroupName;
    private ViewModel viewModel;
    private ViewBinding viewBinding;
    private final List<ScreenParameter> screenParameters = new ArrayList<>();
    private final List<StatePersisted> persistedValues = new ArrayList<>();
    private final List<BindView> bindViews = new ArrayList<>();

    public FragmentMetadata(ClassDefinition fragmentClassDefinition, List<AnnotatedElement> annotatedElements) {
        super(fragmentClassDefinition);
        extractValues(annotatedElements);
    }

    private void extractValues(List<AnnotatedElement> annotatedElements) {
        for (AnnotatedElement annotatedElem : annotatedElements) {
            Element element = annotatedElem.getElement();
            Annotation annotation = annotatedElem.getAnnotation();
            if (annotation instanceof LightbulbFragment lightbulbFragment) {
                layoutName = lightbulbFragment.layoutName();
            } else if (annotation instanceof FragmentScreen fragmentScreen) {
                screenName = fragmentScreen.screenName();
                screenGroupName = fragmentScreen.screenGroup();
            } else if (annotation instanceof FragmentParameter fragmentParameterAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                ScreenParameter parameter = new ScreenParameter(fieldDefinition, fragmentParameterAnnotation);
                screenParameters.add(parameter);
            } else if (annotation instanceof FragmentStatePersisted fragmentStatePersistedAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                persistedValues.add(new StatePersisted(fieldDefinition, fragmentStatePersistedAnnotation));
            } else if (annotation instanceof com.github.rooneyandshadows.lightbulb.apt.annotations.BindView bindViewAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                BindView bView = new BindView(fieldDefinition, bindViewAnnotation);
                bindViews.add(bView);
            } else if (annotation instanceof FragmentViewModel viewModelAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                viewModel = new ViewModel(fieldDefinition, viewModelAnnotation);
            } else if (annotation instanceof FragmentViewBinding viewBindingAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                viewBinding = new ViewBinding(fieldDefinition, viewBindingAnnotation);
            }
        }
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

    public List<ScreenParameter> getScreenParameters() {
        return screenParameters;
    }

    public List<StatePersisted> getPersistedValues() {
        return persistedValues;
    }

    public List<BindView> getBindViews() {
        return bindViews;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public ViewBinding getViewBinding() {
        return viewBinding;
    }

    public boolean hasBindViews() {
        return !bindViews.isEmpty();
    }

    public boolean hasPersistedValues() {
        return !persistedValues.isEmpty();
    }

    public boolean hasParameters() {
        return !screenParameters.isEmpty();
    }

    public boolean hasOptionalParameters() {
        return screenParameters.stream().anyMatch(ScreenParameter::isOptional);
    }

    public boolean hasViewBinding() {
        return viewBinding != null;
    }

    public boolean hasViewModel() {
        return viewModel != null;
    }

    public List<ScreenParameter> getScreenParameters(boolean includeOptional) {
        return includeOptional ? screenParameters : screenParameters.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public String getScreenGroupName() {
        if ((screenGroupName == null || screenGroupName.isEmpty())) {
            return "Common";
        }
        return screenGroupName;
    }

    public static final class ScreenParameter extends FieldMetadata {
        private final boolean optional;

        public ScreenParameter(FieldDefinition fieldDefinition, FragmentParameter annotation) {
            super(fieldDefinition);
            this.optional = annotation.optional();
        }

        public boolean isOptional() {
            return optional;
        }
    }

    public static final class StatePersisted extends FieldMetadata {
        public StatePersisted(FieldDefinition fieldDefinition, FragmentStatePersisted annotation) {
            super(fieldDefinition);
        }
    }

    public static final class BindView extends FieldMetadata {
        private final String resourceName;

        public BindView(FieldDefinition fieldDefinition, com.github.rooneyandshadows.lightbulb.apt.annotations.BindView annotation) {
            super(fieldDefinition);
            this.resourceName = annotation.name();
        }

        public String getResourceName() {
            return resourceName;
        }
    }

    public static final class ViewBinding extends FieldMetadata {
        private final String layoutName;

        public ViewBinding(FieldDefinition fieldDefinition, FragmentViewBinding annotation) {
            super(fieldDefinition);
            this.layoutName = annotation.layoutName();
        }

        public String getLayoutName() {
            return layoutName;
        }
    }

    public static final class ViewModel extends FieldMetadata {
        public ViewModel(FieldDefinition fieldDefinition, FragmentViewModel annotation) {
            super(fieldDefinition);
        }
    }
}