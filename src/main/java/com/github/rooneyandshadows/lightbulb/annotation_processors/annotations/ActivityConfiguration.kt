package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class ActivityConfiguration(val enableRouterGeneration: Boolean = true)