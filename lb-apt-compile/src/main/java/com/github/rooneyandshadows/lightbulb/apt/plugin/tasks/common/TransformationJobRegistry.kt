package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import org.gradle.api.file.FileCollection
import java.util.jar.JarOutputStream

class TransformationJobRegistry(
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: (() -> FileCollection),
) {
    private val transformations: MutableList<TransformationJob> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = TransformationJob(globalClassPath, transformationsClassPath, transformer)
        transformations.add(transformation)
    }

    fun execute(jarDestination: JarOutputStream) {
        transformations.forEach { it.execute(jarDestination) }
    }
}