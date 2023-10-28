package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

class Transformations(
    private val project: Project,
    private val classesDir: FileCollection,
    private val destinationDir: File
) {
    private val transformations: MutableList<Transformation> = mutableListOf()

    fun register(transformation: IClassTransformer) {
        transformations.add(Transformation(project, transformation, classesDir, destinationDir))
    }

    fun execute(){
        transformations.forEach {
            it.execute()
        }
    }
}