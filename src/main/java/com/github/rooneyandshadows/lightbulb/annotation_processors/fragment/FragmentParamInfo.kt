package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment

import com.squareup.kotlinpoet.TypeName

class FragmentParamInfo(
    val name: String,
    val type: TypeName,
    val isOptional: Boolean
)