package com.github.rooneyandshadows.lightbulb.annotation_processors.reader;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentBindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.ScreenInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static javax.lang.model.element.ElementKind.*;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<FragmentBindingData> fragmentBindings = new ArrayList<>();

    public FragmentAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    public List<FragmentBindingData> getFragmentBindings() {
        return fragmentBindings;
    }

    @Override
    protected void onAnnotationsExtracted(Map<Element, List<AnnotatedElement>> annotations) {
        for (Map.Entry<Element, List<AnnotatedElement>> entry : annotations.entrySet()) {
            Element fragmentClassElement = entry.getKey();
            List<AnnotatedElement> annotatedElements = entry.getValue();

            FragmentBindingData bindingData = new FragmentBindingData(elements, fragmentClassElement, annotatedElements);
            fragmentBindings.add(bindingData);
            resultsRegistry.setResult(FRAGMENT_BINDINGS, fragmentBindings);
        }
    }


    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(FragmentConfiguration.class, CLASS);
        targets.put(FragmentScreen.class, CLASS);
        targets.put(FragmentParameter.class, FIELD);
        targets.put(FragmentStatePersisted.class, FIELD);
        targets.put(FragmentBindView.class, FIELD);
        return targets;
    }
}
