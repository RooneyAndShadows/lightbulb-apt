package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class FragmentScreen(val screenName: String = "", val screenGroup: String = "")