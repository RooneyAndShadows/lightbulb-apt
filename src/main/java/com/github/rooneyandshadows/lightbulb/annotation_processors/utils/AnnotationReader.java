package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.generateFragmentClassName;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.*;

public class AnnotationReader {
    private final Messager messager;
    private final Elements elements;
    private final List<FragmentInfo> fragmentInfoList = new ArrayList<>();
    private final List<FragmentScreenGroup> screenGroups = new ArrayList<>();

    public AnnotationReader(Messager messager, Elements elements) {
        this.messager = messager;
        this.elements = elements;
    }

    public List<FragmentInfo> getFragmentInfoList() {
        return fragmentInfoList;
    }

    public List<FragmentScreenGroup> getScreenGroups() {
        return screenGroups;
    }

    public boolean obtainAnnotatedClassesWithFragmentScreen(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentScreen.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentScreen should be on top of fragment classes.");
                return false;
            }
            FragmentScreen annotation = classElement.getAnnotation(FragmentScreen.class);
            FragmentInfo fragmentInfo = getOrCreateClassInfoForElement(classElement);
            fragmentInfo.setScreenName(annotation.screenName());
            CreateOrUpdateScreenGroup(fragmentInfo, annotation.screenGroup());
        }
        return true;
    }

    public boolean obtainAnnotatedFieldsWithFragmentParameter(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentParameter.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentParameter should be on top of fragment field.");
                return false;
            }
            FragmentParameter annotation = element.getAnnotation(FragmentParameter.class);
            Element classElement = element.getEnclosingElement();
            FragmentInfo fragmentInfo = getOrCreateClassInfoForElement(classElement);
            FragmentParamInfo info = new FragmentParamInfo(element.getSimpleName().toString(), getTypeOfFieldElement(element), annotation.optional());
            fragmentInfo.getFragmentParameters().add(info);
        }
        return true;
    }

    public boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            FragmentInfo fragmentInfo = getOrCreateClassInfoForElement(classElement);
            fragmentInfo.setConfigAnnotation(classElement.getAnnotation(FragmentConfiguration.class));
        }
        return true;
    }

    public boolean obtainAnnotatedFieldsWithBindView(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.");
                return false;
            }
            Element classElement = element.getEnclosingElement();
            FragmentInfo classInfo = getOrCreateClassInfoForElement(classElement);
            BindView annotation = element.getAnnotation(BindView.class);
            classInfo.getViewBindings().put(element.getSimpleName().toString(), annotation.name());
        }
        return true;
    }

    private void CreateOrUpdateScreenGroup(FragmentInfo fragmentInfo, String screenGroup) {
        FragmentScreenGroup group = screenGroups.stream().filter(info -> info.getScreenGroupName().equals(screenGroup))
                .findFirst()
                .orElse(null);
        if (group == null) {
            group = new FragmentScreenGroup(screenGroup);
            screenGroups.add(group);
        }
        group.addScreen(fragmentInfo);
    }

    private FragmentInfo getOrCreateClassInfoForElement(Element classElement) {
        String canonicalName = getFullClassName(elements, classElement);
        FragmentInfo existingClassInfo = fragmentInfoList.stream().filter(info -> info.getClassName().canonicalName().equals(canonicalName))
                .findFirst()
                .orElse(null);
        FragmentInfo fragmentInformation;
        if (existingClassInfo == null) {
            fragmentInformation = new FragmentInfo();
            fragmentInformation.setType(classElement.asType());
            fragmentInformation.setCanBeInstantiated(canBeInstantiated(classElement));
            fragmentInformation.setClassName(generateFragmentClassName(classElement, elements));
            fragmentInfoList.add(fragmentInformation);
        } else {
            fragmentInformation = existingClassInfo;
        }
        return fragmentInformation;
    }
}