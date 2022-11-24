package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class FragmentConfiguration(
    val layoutName: String = "",
    val isMainScreenFragment: Boolean = true,
    val hasLeftDrawer: Boolean = false,
    val hasOptionsMenu: Boolean = false
)