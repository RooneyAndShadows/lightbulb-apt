package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class BindView(val name: String = "")