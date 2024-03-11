package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.MethodMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.MethodDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class FragmentMetadata extends ClassMetadata {
    private String layoutName;
    private String screenName;
    private String screenGroupName;
    private ViewModelMetadata viewModel;
    private ViewBindingMetadata viewBinding;
    private final List<ScreenParameterMetadata> screenParameters = new ArrayList<>();
    private final List<StatePersistedMetadata> persistedValues = new ArrayList<>();
    private final List<BindViewMetadata> bindViews = new ArrayList<>();
    private final List<ResultListenerMetadata> resultListeners = new ArrayList<>();

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
                ScreenParameterMetadata parameter = new ScreenParameterMetadata(fieldDefinition, fragmentParameterAnnotation);
                screenParameters.add(parameter);
            } else if (annotation instanceof FragmentStatePersisted fragmentStatePersistedAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                persistedValues.add(new StatePersistedMetadata(fieldDefinition, fragmentStatePersistedAnnotation));
            } else if (annotation instanceof com.github.rooneyandshadows.lightbulb.apt.annotations.BindView bindViewAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                BindViewMetadata bView = new BindViewMetadata(fieldDefinition, bindViewAnnotation);
                bindViews.add(bView);
            } else if (annotation instanceof FragmentViewModel viewModelAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                viewModel = new ViewModelMetadata(fieldDefinition, viewModelAnnotation);
            } else if (annotation instanceof FragmentViewBinding viewBindingAnnotation) {
                FieldDefinition fieldDefinition = new FieldDefinition((VariableElement) element);
                viewBinding = new ViewBindingMetadata(fieldDefinition, viewBindingAnnotation);
            } else if (annotation instanceof ResultListener resultListenerAnnotation) {
                MethodDefinition methodDefinition = new MethodDefinition((ExecutableElement) element);
                resultListeners.add(new ResultListenerMetadata(methodDefinition, resultListenerAnnotation));
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

    public List<ScreenParameterMetadata> getScreenParameters() {
        return screenParameters;
    }

    public List<StatePersistedMetadata> getPersistedValues() {
        return persistedValues;
    }

    public List<BindViewMetadata> getBindViews() {
        return bindViews;
    }

    public ViewModelMetadata getViewModel() {
        return viewModel;
    }

    public ViewBindingMetadata getViewBinding() {
        return viewBinding;
    }

    public List<ResultListenerMetadata> getResultListeners() {
        return resultListeners;
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

    public boolean hasResultListeners() {
        return !resultListeners.isEmpty();
    }

    public boolean hasOptionalParameters() {
        return screenParameters.stream().anyMatch(ScreenParameterMetadata::isOptional);
    }

    public boolean hasViewBinding() {
        return viewBinding != null;
    }

    public boolean hasViewModel() {
        return viewModel != null;
    }

    public List<ScreenParameterMetadata> getScreenParameters(boolean includeOptional) {
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

    public static final class ScreenParameterMetadata extends FieldMetadata {
        private final boolean optional;

        public ScreenParameterMetadata(FieldDefinition fieldDefinition, FragmentParameter annotation) {
            super(fieldDefinition);
            this.optional = annotation.optional();
        }

        public boolean isOptional() {
            return optional;
        }
    }

    public static final class StatePersistedMetadata extends FieldMetadata {
        public StatePersistedMetadata(FieldDefinition fieldDefinition, FragmentStatePersisted annotation) {
            super(fieldDefinition);
        }
    }

    public static final class BindViewMetadata extends FieldMetadata {
        private final String resourceName;

        public BindViewMetadata(FieldDefinition fieldDefinition, com.github.rooneyandshadows.lightbulb.apt.annotations.BindView annotation) {
            super(fieldDefinition);
            this.resourceName = annotation.name();
        }

        public String getResourceName() {
            return resourceName;
        }
    }

    public static final class ViewBindingMetadata extends FieldMetadata {
        private final String layoutName;

        public ViewBindingMetadata(FieldDefinition fieldDefinition, FragmentViewBinding annotation) {
            super(fieldDefinition);
            this.layoutName = annotation.layoutName();
        }

        public String getLayoutName() {
            return layoutName;
        }
    }

    public static final class ViewModelMetadata extends FieldMetadata {
        public ViewModelMetadata(FieldDefinition fieldDefinition, FragmentViewModel annotation) {
            super(fieldDefinition);
        }
    }

    public static final class ResultListenerMetadata extends MethodMetadata {
        public ResultListenerMetadata(MethodDefinition methodDefinition, ResultListener annotation) {
            super(methodDefinition);
        }
    }
}