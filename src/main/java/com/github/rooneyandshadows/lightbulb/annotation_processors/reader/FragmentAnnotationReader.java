package com.github.rooneyandshadows.lightbulb.annotation_processors.reader;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentBindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.ScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<FragmentBindingData> fragmentBindings = new ArrayList<>();
    private final List<ScreenGroup> screenGroups = new ArrayList<>();

    public FragmentAnnotationReader(Messager messager, Elements elements, RoundEnvironment environment) {
        super(messager, elements, environment);
    }

    public List<FragmentBindingData> getFragmentBindings() {
        return fragmentBindings;
    }

    public List<ScreenGroup> getScreenGroups() {
        return screenGroups;
    }

    @Override
    protected void onAnnotationsExtracted(Map<Element, List<AnnotatedElement>> annotations) {
        for (Map.Entry<Element, List<AnnotatedElement>> entry : annotations.entrySet()) {
            Element fragmentClassElement = entry.getKey();
            List<AnnotatedElement> annotatedElements = entry.getValue();

            FragmentBindingData bindingData = new FragmentBindingData(elements, fragmentClassElement, annotatedElements);
            fragmentBindings.add(bindingData);
        }
    }


    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(FragmentConfiguration.class, ElementKind.CLASS);
        targets.put(FragmentScreen.class, ElementKind.CLASS);
        targets.put(FragmentParameter.class, ElementKind.FIELD);
        targets.put(FragmentStatePersisted.class, ElementKind.FIELD);
        targets.put(FragmentBindView.class, ElementKind.FIELD);
        return targets;
    }
}
