package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata.BindView;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_FRAGMENT_DESCRIPTION;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.FIELD;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<FragmentMetadata> fragmentsMetadata = new ArrayList<>();

    public FragmentAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        String layoutName = null;
        String screenName = null;
        String screenGroupName = null;
        List<ScreenParameter> screenParameters = new ArrayList<>();
        List<StatePersisted> persistedValues = new ArrayList<>();
        List<BindView> bindViews = new ArrayList<>();
        List<ViewBinding> viewBindings = new ArrayList<>();
        List<ViewModel> viewModels = new ArrayList<>();

        for (AnnotatedElement annotatedElem : annotatedElements) {
            Annotation annotation = annotatedElem.getAnnotation();
            if (annotation instanceof LightbulbFragment lightbulbFragment) {
                layoutName = lightbulbFragment.layoutName();
            } else if (annotation instanceof FragmentScreen fragmentScreen) {
                screenName = fragmentScreen.screenName();
                screenGroupName = fragmentScreen.screenGroup();
            } else if (annotation instanceof FragmentParameter fragmentParameter) {
                ScreenParameter parameter = new ScreenParameter((VariableElement) annotatedElem.getElement(), fragmentParameter.optional());
                screenParameters.add(parameter);
            } else if (annotation instanceof FragmentStatePersisted fragmentStatePersisted) {
                StatePersisted statePersisted = new StatePersisted((VariableElement) annotatedElem.getElement());
                persistedValues.add(statePersisted);
            } else if (annotation instanceof com.github.rooneyandshadows.lightbulb.apt.annotations.BindView bindView) {
                BindView bView = new BindView((VariableElement) annotatedElem.getElement(), bindView.name());
                bindViews.add(bView);
            } else if (annotation instanceof FragmentViewModel viewModel) {
                ViewModel vModel = new ViewModel((VariableElement) annotatedElem.getElement());
                viewModels.add(vModel);
            } else if (annotation instanceof FragmentViewBinding viewBinding) {
                ViewBinding vBinding = new ViewBinding((VariableElement) annotatedElem.getElement(), viewBinding.layoutName());
                viewBindings.add(vBinding);
            }
        }

        FragmentMetadata metadata = new FragmentMetadata(
                target,
                layoutName,
                screenName,
                screenGroupName,
                screenParameters,
                persistedValues,
                bindViews,
                viewModels,
                viewBindings
        );


        fragmentsMetadata.add(metadata);
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_FRAGMENT_DESCRIPTION, fragmentsMetadata);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbFragment.class, CLASS);
        targets.put(FragmentScreen.class, CLASS);
        targets.put(FragmentParameter.class, FIELD);
        targets.put(FragmentStatePersisted.class, FIELD);
        targets.put(FragmentViewBinding.class, FIELD);
        targets.put(FragmentViewModel.class, FIELD);
        targets.put(com.github.rooneyandshadows.lightbulb.apt.annotations.BindView.class, FIELD);
        return targets;
    }
}
