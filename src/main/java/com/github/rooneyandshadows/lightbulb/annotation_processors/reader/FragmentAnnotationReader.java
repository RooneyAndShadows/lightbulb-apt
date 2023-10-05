package com.github.rooneyandshadows.lightbulb.annotation_processors.reader;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentBindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getFullClassName;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<FragmentInfo> fragmentInfoList = new ArrayList<>();
    private final List<FragmentScreenGroup> screenGroups = new ArrayList<>();
    private final Map<String, FragmentConfiguration> fragmentConfigurationsList = new HashMap<>();
    private final Map<String, FragmentScreen> fragmentScreensList = new HashMap<>();
    private final Map<String, Map<String, FragmentParameter>> fragmentParameters = new HashMap<>();
    private final Map<String, Map<String, FragmentStatePersisted>> fragmentStatePersisted = new HashMap<>();
    private final Map<String, Map<String, FragmentBindView>> fragmentBindView = new HashMap<>();

    public FragmentAnnotationReader(Messager messager, Elements elements, RoundEnvironment environment) {
        super(messager, elements, environment);
    }

    public List<FragmentInfo> getFragmentInfoList() {
        return fragmentInfoList;
    }

    public List<FragmentScreenGroup> getScreenGroups() {
        return screenGroups;
    }


    public boolean readAnnotations() {
        boolean result = obtainAnnotations(FragmentConfiguration.class, ElementKind.CLASS, (targetElementSimpleName, enclosingClassName, annotation) -> {
            fragmentConfigurationsList.putIfAbsent(enclosingClassName, annotation);
        });
        result &= obtainAnnotations(FragmentScreen.class, ElementKind.CLASS, (targetElementSimpleName, enclosingClassName, annotation) -> {
            fragmentScreensList.putIfAbsent(enclosingClassName, annotation);
        });
        result &= obtainAnnotations(FragmentParameter.class, ElementKind.FIELD, (targetElementSimpleName, enclosingClassName, annotation) -> {
            Map<String, FragmentParameter> value = fragmentParameters.computeIfAbsent(enclosingClassName, s -> new HashMap<>());
            value.putIfAbsent(targetElementSimpleName, annotation);
        });
        result &= obtainAnnotations(FragmentStatePersisted.class, ElementKind.FIELD, (targetElementSimpleName, enclosingClassName, annotation) -> {
            Map<String, FragmentStatePersisted> value = fragmentStatePersisted.computeIfAbsent(enclosingClassName, s -> new HashMap<>());
            value.putIfAbsent(targetElementSimpleName, annotation);
        });
        result &= obtainAnnotations(FragmentBindView.class, ElementKind.FIELD, (targetElementSimpleName, enclosingClassName, annotation) -> {
            Map<String, FragmentBindView> value = fragmentBindView.computeIfAbsent(enclosingClassName, s -> new HashMap<>());
            value.putIfAbsent(targetElementSimpleName, annotation);
        });
        return result;
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
            fragmentInformation = new FragmentInfo(classElement);
            fragmentInfoList.add(fragmentInformation);
        } else {
            fragmentInformation = existingClassInfo;
        }
        return fragmentInformation;
    }
}
