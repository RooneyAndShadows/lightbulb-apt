package com.github.rooneyandshadows.lightbulb.annotation_processors

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils
import com.squareup.kotlinpoet.ClassName
import java.time.OffsetDateTime
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

//PACKAGES
const val LB_ROUTING = "com.github.rooneyandshadows.lightbulb.application.activity.routing"
const val LB_ACTIVITY = "com.github.rooneyandshadows.lightbulb.application.activity"
const val LB_FRAGMENT = "com.github.rooneyandshadows.lightbulb.application.fragment.base"
const val ROONEY_AND_SHADOWS_DATE = "com.github.rooneyandshadows.java.commons.date"
const val ANDROID_OS = "android.os"
const val JAVA_UTIL = "java.util"
const val JAVA_LANG = "java.lang"
const val JAVA_TIME = "java.time"

//SUPPORTED OPTIONS
val ROOT_PACKAGE: String = "lightbulb.generate.root.package"

//CLASS NAMES
val BASE_FRAGMENT: ClassName = ClassName(LB_FRAGMENT, "BaseFragment")
val BASE_FRAGMENT_CONFIGURATION: ClassName = BASE_FRAGMENT.nestedClass("Configuration")
val BASE_ACTIVITY: ClassName = ClassName(LB_ACTIVITY, "BaseActivity")
val BASE_ROUTER: ClassName = ClassName(LB_ROUTING, "BaseActivityRouter")
val DATE_UTILS: ClassName = ClassName(ROONEY_AND_SHADOWS_DATE, "DateUtils")
val OFFSET_DATE_UTILS: ClassName = ClassName(ROONEY_AND_SHADOWS_DATE, "DateUtilsOffsetDate")
val BUNDLE: ClassName = ClassName(ANDROID_OS, "Bundle")
val UUID: ClassName = ClassName(JAVA_UTIL, "UUID")

//TYPES
const val stringType: String = "java.lang.String"
const val intType: String = "java.lang.Integer"
const val intPrimType: String = "int"
const val booleanType: String = "java.lang.Boolean"
const val booleanPrimType: String = "boolean"
val uuidType: String = java.util.UUID::class.java.canonicalName
const val floatType: String = "java.lang.Float"
const val floatPrimType: String = "float"
const val longType: String = "java.lang.Long"
const val longPrimType: String = "long"
const val doubleType: String = "java.lang.Double"
const val doublePrimType: String = "double"
val dateType: String = Date::class.java.canonicalName
val OffsetDateType: String = OffsetDateTime::class.java.canonicalName

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