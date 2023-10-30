package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File
import java.nio.file.Paths

class Transformations(private val project: Project) {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "transformations"
    private val rootDestinationDir: String = Paths.get(buildDir, destinationRoot).toString()
    private val sourceSetOutputs: List<SourceSetOutput> = SourceSetOutput.from(project)
    private val outputFileCollections: List<FileCollection> = SourceSetOutput.outputCollections(sourceSetOutputs)
    private val transformations: MutableList<Transformation> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = Transformation(buildDir, rootDestinationDir, outputFileCollections, transformer)
        transformations.add(transformation)
    }

    fun execute() {
        transformations.forEach { it.execute() }
    }

    fun getRootDestinationDir(): File {
        return project.file(rootDestinationDir)
    }

    fun getTransformationClassFiles(): List<File> {
        return SourceSetOutput.classFiles(sourceSetOutputs)
    }
}