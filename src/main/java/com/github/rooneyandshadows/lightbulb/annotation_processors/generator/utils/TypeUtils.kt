package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo
import com.squareup.kotlinpoet.ClassName
import java.time.OffsetDateTime
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

internal object TypeUtils {
    const val stringType: String = "java.lang.String"
    const val intType: String = "java.lang.Integer"
    const val intPrimType: String = "int"
    const val booleanType: String = "java.lang.Boolean"
    const val booleanPrimType: String = "boolean"
    const val uuidType: String = "java.util.UUID"
    const val floatType: String = "java.lang.Float"
    const val floatPrimType: String = "float"
    const val longType: String = "java.lang.Long"
    const val longPrimType: String = "long"
    const val doubleType: String = "java.lang.Double"
    const val doublePrimType: String = "double"
    const val dateType: String = "java.util.Date"
    const val OffsetDateType: String = "java.time.OffsetDateTime"

    private val simpleTypesList = listOf(
        String::class.qualifiedName, Int::class.qualifiedName, Boolean::class.qualifiedName, uuidType,
        Float::class.qualifiedName, Long::class.qualifiedName, Double::class.qualifiedName
    )

    fun isSimpleType(canonicalName: String): Boolean {
        return simpleTypesList.contains(canonicalName)
    }

    fun generateMappedFragmentBindingClassName(fragmentInfo: FragmentInfo, className: String): ClassName {
        return ClassName(fragmentInfo.className!!.packageName, className)
    }

    fun generateFragmentClassName(fragmentClass: Element, elements: Elements): ClassName {
        val classPackage: String = ElementUtils.getPackage(elements, fragmentClass)
        return ClassName(classPackage, fragmentClass.simpleName.toString())
    }
}