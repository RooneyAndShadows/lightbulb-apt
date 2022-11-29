package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.github.rooneyandshadows.lightbulb.annotation_processors.activity.ActivityInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.*;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.generateFragmentClassName;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.*;

public class AnnotationReader {
    private final Messager messager;
    private final Elements elements;
    private final List<ActivityInfo> activityInfoList = new ArrayList<>();
    private final List<FragmentInfo> fragmentInfoList = new ArrayList<>();
    private final List<FragmentScreenGroup> screenGroups = new ArrayList<>();

    public AnnotationReader(Messager messager, Elements elements) {
        this.messager = messager;
        this.elements = elements;
    }

    public List<ActivityInfo> getActivityInfoList() {
        return activityInfoList;
    }

    public List<FragmentInfo> getFragmentInfoList() {
        return fragmentInfoList;
    }

    public List<FragmentScreenGroup> getScreenGroups() {
        return screenGroups;
    }

    public boolean obtainAnnotatedClassesWithActivityConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(ActivityConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ActivityConfiguration should be on top of activity classes.");
                return false;
            }
            ActivityConfiguration annotation = classElement.getAnnotation(ActivityConfiguration.class);
            ActivityInfo activityInfo = getOrCreateActivityInfo(classElement);
            activityInfo.setRoutingEnabled(annotation.enableRouterGeneration());
        }
        return true;
    }

    public boolean obtainAnnotatedClassesWithFragmentScreen(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentScreen.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentScreen should be on top of fragment classes.");
                return false;
            }
            FragmentScreen annotation = classElement.getAnnotation(FragmentScreen.class);
            FragmentInfo fragmentInfo = getOrCreateFragmentInfo(classElement);
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
            FragmentInfo fragmentInfo = getOrCreateFragmentInfo(classElement);
            FragmentParamInfo info = new FragmentParamInfo(element, annotation);
            fragmentInfo.getFragmentParameters().add(info);
        }
        return true;
    }

    public boolean obtainAnnotatedFieldsWithFragmentStatePersisted(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentStatePersisted.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentStatePersisted should be on top of fragment field.");
                return false;
            }
            FragmentStatePersisted annotation = element.getAnnotation(FragmentStatePersisted.class);
            Element classElement = element.getEnclosingElement();
            FragmentInfo fragmentInfo = getOrCreateFragmentInfo(classElement);
            FragmentVariableInfo info = new FragmentVariableInfo(element);
            fragmentInfo.getFragmentPersistedVariables().add(info);
        }
        return true;
    }

    public boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            FragmentInfo fragmentInfo = getOrCreateFragmentInfo(classElement);
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
            FragmentInfo classInfo = getOrCreateFragmentInfo(classElement);
            FragmentViewBindingInfo viewBindingInfo = new FragmentViewBindingInfo(element, element.getAnnotation(BindView.class));
            classInfo.getViewBindings().add(viewBindingInfo);
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

    private FragmentInfo getOrCreateFragmentInfo(Element classElement) {
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

    private ActivityInfo getOrCreateActivityInfo(Element classElement) {
        String canonicalName = getFullClassName(elements, classElement);
        ActivityInfo existingClassInfo = activityInfoList.stream().filter(info -> info.getClassName().canonicalName().equals(canonicalName))
                .findFirst()
                .orElse(null);
        ActivityInfo activityInformation;
        if (existingClassInfo == null) {
            activityInformation = new ActivityInfo();
            activityInformation.setClassName(generateFragmentClassName(classElement, elements));
            activityInfoList.add(activityInformation);
        } else {
            activityInformation = existingClassInfo;
        }
        return activityInformation;
    }
}
