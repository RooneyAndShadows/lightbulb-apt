package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.base.IClassTransformer
import org.gradle.api.file.FileCollection

class TransformationJobRegistry(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: FileCollection,
) {
    private val transformations: MutableList<TransformationJob> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = TransformationJob(
            buildDir,
            rootDestinationDir,
            globalClassPath,
            transformationsClassPath,
            transformer
        )
        transformations.add(transformation)
    }

    fun execute() {
        transformations.forEach { it.execute() }
    }
}