package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import org.gradle.api.file.FileCollection

class TransformationRegistry(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val outputFileCollections: List<FileCollection>
) {
    private val transformations: MutableList<Transformation> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = Transformation(buildDir, rootDestinationDir, outputFileCollections, transformer)
        transformations.add(transformation)
    }

    fun execute() {
        transformations.forEach { it.execute() }
    }
}