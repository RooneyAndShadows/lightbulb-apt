package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

class Transformations(
    private val project: Project,
    private val sourceSets: SourceSetContainer,
    private val classesDir: FileCollection,
    private val destinationDir: File
) {
    private val transformations: MutableList<Transformation> = mutableListOf()

    fun register(transformation: IClassTransformer) {
        classesDir.forEach { dir ->
            val inputDirectory = dir.path
            println("============".plus(dir))
            transformations.add(Transformation(project, inputDirectory, destinationDir, transformation))
        }
    }

    fun execute() {
        transformations.forEach { it.execute() }
        classesDir.forEach { dir ->
            val inputDirectory = dir.path
            copyTransformations()
        }

    }

    private fun copyTransformations() {
        println(classesDir.asPath)
    }
}