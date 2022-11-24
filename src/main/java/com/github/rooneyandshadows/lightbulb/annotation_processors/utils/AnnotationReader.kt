package com.github.rooneyandshadows.lightbulb.annotation_processors.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.activity.ActivityInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup
import com.github.rooneyandshadows.lightbulb.annotation_processors.generateFragmentClassName
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.canBeInstantiated
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getFullClassName
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getTypeOfFieldElement
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class AnnotationReader(private val messager: Messager, private val elements: Elements) {
    val activityInfoList: MutableList<ActivityInfo> = ArrayList()
    val fragmentInfoList: MutableList<FragmentInfo> = ArrayList()
    val screenGroups: MutableList<FragmentScreenGroup> = ArrayList()

    fun obtainAnnotatedClassesWithActivityConfiguration(roundEnvironment: RoundEnvironment): Boolean {
        for (classElement in roundEnvironment.getElementsAnnotatedWith(
            ActivityConfiguration::class.java
        )) {
            if (classElement.kind != ElementKind.CLASS) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "@ActivityConfiguration should be on top of activity classes."
                )
                return false
            }
            val annotation = classElement.getAnnotation(
                ActivityConfiguration::class.java
            )
            val activityInfo = getOrCreateActivityInfo(classElement)
            activityInfo.isRoutingEnabled = annotation.enableRouterGeneration
        }
        return true
    }

    fun obtainAnnotatedClassesWithFragmentScreen(roundEnvironment: RoundEnvironment): Boolean {
        for (classElement in roundEnvironment.getElementsAnnotatedWith(FragmentScreen::class.java)) {
            if (classElement.kind != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentScreen should be on top of fragment classes.")
                return false
            }
            val annotation = classElement.getAnnotation(FragmentScreen::class.java)
            val fragmentInfo = getOrCreateFragmentInfo(classElement)
            fragmentInfo.screenName = annotation.screenName
            CreateOrUpdateScreenGroup(fragmentInfo, annotation.screenGroup)
        }
        return true
    }

    fun obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment: RoundEnvironment): Boolean {
        for (element in roundEnvironment.getElementsAnnotatedWith(FragmentParameter::class.java)) {
            if (element.kind != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentParameter should be on top of fragment field.")
                return false
            }
            val annotation = element.getAnnotation(FragmentParameter::class.java)
            val classElement = element.enclosingElement
            val fragmentInfo = getOrCreateFragmentInfo(classElement)
            val info =
                FragmentParamInfo(element.simpleName.toString(), getTypeOfFieldElement(element), annotation.optional)
            fragmentInfo.fragmentParameters.add(info)
        }
        return true
    }

    fun obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment: RoundEnvironment): Boolean {
        for (classElement in roundEnvironment.getElementsAnnotatedWith(
            FragmentConfiguration::class.java
        )) {
            if (classElement.kind != ElementKind.CLASS) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "@FragmentConfiguration should be on top of fragment classes."
                )
                return false
            }
            val fragmentInfo = getOrCreateFragmentInfo(classElement)
            fragmentInfo.configAnnotation = classElement.getAnnotation(FragmentConfiguration::class.java)
        }
        return true
    }

    fun obtainAnnotatedFieldsWithBindView(roundEnvironment: RoundEnvironment): Boolean {
        for (element in roundEnvironment.getElementsAnnotatedWith(BindView::class.java)) {
            if (element.kind != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.")
                return false
            }
            val classElement = element.enclosingElement
            val classInfo = getOrCreateFragmentInfo(classElement)
            val annotation = element.getAnnotation(BindView::class.java)
            classInfo.viewBindings[element.simpleName.toString()] = annotation.name
        }
        return true
    }

    private fun CreateOrUpdateScreenGroup(fragmentInfo: FragmentInfo, screenGroup: String) {
        var group = screenGroups.stream().filter { info: FragmentScreenGroup? -> info!!.screenGroupName == screenGroup }
            .findFirst()
            .orElse(null)
        if (group == null) {
            group = FragmentScreenGroup(screenGroup)
            screenGroups.add(group)
        }
        group.addScreen(fragmentInfo)
    }

    private fun getOrCreateFragmentInfo(classElement: Element): FragmentInfo {
        val canonicalName = getFullClassName(elements, classElement)
        val existingClassInfo = fragmentInfoList.stream()
            .filter { info: FragmentInfo? -> info!!.className!!.canonicalName == canonicalName }
            .findFirst()
            .orElse(null)
        val fragmentInformation: FragmentInfo
        if (existingClassInfo == null) {
            fragmentInformation = FragmentInfo()
            fragmentInformation.type = classElement.asType()
            fragmentInformation.isCanBeInstantiated = canBeInstantiated(classElement)
            fragmentInformation.className = generateFragmentClassName(classElement, elements)
            fragmentInfoList.add(fragmentInformation)
        } else {
            fragmentInformation = existingClassInfo
        }
        return fragmentInformation
    }

    private fun getOrCreateActivityInfo(classElement: Element): ActivityInfo {
        val canonicalName = getFullClassName(elements, classElement)
        val existingClassInfo = activityInfoList.stream()
            .filter { info: ActivityInfo? -> info!!.className!!.canonicalName == canonicalName }
            .findFirst()
            .orElse(null)
        val activityInformation: ActivityInfo
        if (existingClassInfo == null) {
            activityInformation = ActivityInfo()
            activityInformation.className = generateFragmentClassName(classElement, elements)
            activityInfoList.add(activityInformation)
        } else {
            activityInformation = existingClassInfo
        }
        return activityInformation
    }
}