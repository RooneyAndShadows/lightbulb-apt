package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.ANDROID_OS
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.JAVA_UTIL
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.LB_ACTIVITY
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.LB_FRAGMENT
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.LB_ROUTING
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.utils.Packages.ROONEY_AND_SHADOWS_DATE
import com.squareup.kotlinpoet.ClassName

internal object ClassNames {
    val BASE_FRAGMENT: ClassName = ClassName(LB_FRAGMENT, "BaseFragment")
    val BASE_FRAGMENT_CONFIGURATION: ClassName = BASE_FRAGMENT.nestedClass("Configuration")
    val BASE_ACTIVITY: ClassName = ClassName(LB_ACTIVITY, "BaseActivity")
    val BASE_ROUTER: ClassName = ClassName(LB_ROUTING, "BaseActivityRouter")
    val DATE_UTILS: ClassName = ClassName(ROONEY_AND_SHADOWS_DATE, "DateUtils")
    val OFFSET_DATE_UTILS: ClassName = ClassName(ROONEY_AND_SHADOWS_DATE, "DateUtilsOffsetDate")
    val BUNDLE: ClassName = ClassName(ANDROID_OS, "Bundle")
    val UUID: ClassName = ClassName(JAVA_UTIL, "UUID")
}