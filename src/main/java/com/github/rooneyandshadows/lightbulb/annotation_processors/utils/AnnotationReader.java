package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.ScreenInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.Variable;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.ViewBinding;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.generateClassName;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.*;

public class AnnotationReader {
    private final Messager messager;
    private final Elements elements;
    private final List<ActivityBindingData> activityInfoList = new ArrayList<>();
    private final List<FragmentBindingData> fragmentInfoList = new ArrayList<>();
    private final List<ScreenInfo> screenGroups = new ArrayList<>();

    public AnnotationReader(Messager messager, Elements elements) {
        this.messager = messager;
        this.elements = elements;
    }

    public List<ActivityBindingData> getActivityInfoList() {
        return activityInfoList;
    }

    public List<FragmentBindingData> getFragmentInfoList() {
        return fragmentInfoList;
    }

    public List<ScreenInfo> getScreenGroups() {
        return screenGroups;
    }

    public boolean obtainAnnotatedClassesWithActivityConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(ActivityConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ActivityConfiguration should be on top of activity classes.");
                return false;
            }
            ActivityConfiguration annotation = classElement.getAnnotation(ActivityConfiguration.class);
            ActivityBindingData activityInfo = getOrCreateActivityInfo(classElement);
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
            FragmentBindingData fragmentInfo = getOrCreateFragmentInfo(classElement);
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
            FragmentBindingData fragmentInfo = getOrCreateFragmentInfo(classElement);
            Parameter info = new Parameter(element, annotation, optional);
            fragmentInfo.getParameters().add(info);
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
            FragmentBindingData fragmentInfo = getOrCreateFragmentInfo(classElement);
            Variable info = new Variable(element);
            fragmentInfo.getPersistedVariables().add(info);
        }
        return true;
    }

    public boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            FragmentBindingData fragmentInfo = getOrCreateFragmentInfo(classElement);
            fragmentInfo.setConfigAnnotation(classElement.getAnnotation(FragmentConfiguration.class));
        }
        return true;
    }

    public boolean obtainAnnotatedFieldsWithBindView(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentBindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.");
                return false;
            }
            Element classElement = element.getEnclosingElement();
            FragmentBindingData classInfo = getOrCreateFragmentInfo(classElement);
            ViewBinding viewBindingInfo = new ViewBinding(element, element.getAnnotation(FragmentBindView.class));
            classInfo.getViewBindings().add(viewBindingInfo);
        }
        return true;
    }

    private void CreateOrUpdateScreenGroup(FragmentBindingData fragmentInfo, String screenGroup) {
        ScreenInfo group = screenGroups.stream().filter(info -> info.getScreenGroupName().equals(screenGroup))
                .findFirst()
                .orElse(null);
        if (group == null) {
            group = new ScreenInfo(screenGroup);
            screenGroups.add(group);
        }
        group.addScreen(fragmentInfo);
    }

    private FragmentBindingData getOrCreateFragmentInfo(Element classElement) {
        String canonicalName = getFullClassName(elements, classElement);
        FragmentBindingData existingClassInfo = fragmentInfoList.stream().filter(info -> info.getClassName().canonicalName().equals(canonicalName))
                .findFirst()
                .orElse(null);
        FragmentBindingData fragmentInformation;
        if (existingClassInfo == null) {
            fragmentInformation = new FragmentBindingData(classElement);
            fragmentInfoList.add(fragmentInformation);
        } else {
            fragmentInformation = existingClassInfo;
        }
        return fragmentInformation;
    }

    private ActivityBindingData getOrCreateActivityInfo(Element classElement) {
        String canonicalName = getFullClassName(elements, classElement);
        ActivityBindingData existingClassInfo = activityInfoList.stream().filter(info -> info.getClassName().canonicalName().equals(canonicalName))
                .findFirst()
                .orElse(null);
        ActivityBindingData activityInformation;
        if (existingClassInfo == null) {
            activityInformation = new ActivityBindingData();
            activityInformation.setClassName(generateClassName(classElement, elements));
            activityInfoList.add(activityInformation);
        } else {
            activityInformation = existingClassInfo;
        }
        return activityInformation;
    }
}
