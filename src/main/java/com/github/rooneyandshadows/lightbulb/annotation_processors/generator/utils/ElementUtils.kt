package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.booleanPrimType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.booleanType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.doublePrimType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.doubleType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.floatPrimType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.floatType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.intPrimType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.intType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.longPrimType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.longType
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.TypeUtils.stringType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements

internal object ElementUtils {
    @JvmStatic
    fun getPackage(elements: Elements, element: Element?): String {
        return elements.getPackageOf(element)
            .qualifiedName
            .toString()
    }

    @JvmStatic
    fun getFullClassName(elements: Elements, element: Element): String {
        val classPackage = getPackage(elements, element)
        val classSimpleName = element.simpleName.toString()
        return "$classPackage.$classSimpleName"
    }

    @JvmStatic
    fun getTypeOfFieldElement(element: Element): TypeName {
        var typeName = element.asType().asTypeName()
        when (typeName.toString()) {
            stringType -> {
                typeName = ClassName("kotlin", "String")
            }

            intType, intPrimType -> {
                typeName = ClassName("kotlin", "Int")
            }

            booleanType, booleanPrimType -> {
                typeName = ClassName("kotlin", "Boolean")
            }

            floatType, floatPrimType -> {
                typeName = ClassName("kotlin", "Float")
            }

            longType, longPrimType -> {
                typeName = ClassName("kotlin", "Long")
            }

            doubleType, doublePrimType -> {
                typeName = ClassName("kotlin", "Double")
            }

            else -> {}
        }
        return typeName
    }

    @JvmStatic
    fun canBeInstantiated(classElement: Element): Boolean {
        return classElement.kind == ElementKind.CLASS && !classElement.modifiers.contains(Modifier.ABSTRACT)
    }
}