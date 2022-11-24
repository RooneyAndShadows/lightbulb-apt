package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration
import com.squareup.kotlinpoet.ClassName
import javax.lang.model.type.TypeMirror

class FragmentInfo {
    var type: TypeMirror? = null
    var className: ClassName? = null
    var mappedBindingType: ClassName? = null
    var screenName: String? = null
    var isCanBeInstantiated = false
    var configAnnotation: FragmentConfiguration? = null
    val viewBindings: MutableMap<String, String> = mutableMapOf()
    val fragmentParameters: MutableList<FragmentParamInfo> = mutableListOf()
}