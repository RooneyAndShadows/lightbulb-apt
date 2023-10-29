package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Paths

class Transformations(private val project: Project) {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "transformations"
    val rootDestinationDir: String = Paths.get(buildDir, destinationRoot).toString()
    private val sourceSetOutputs: List<SourceSetOutput>
    val classFiles: List<File>
        get() = sourceSetOutputs.map { return@map it.classFiles }.flatten()
    val outputFileCollections: List<FileCollection>
        get() = sourceSetOutputs.map { return@map it.outputCollections }.flatten()

    private val transformations: MutableList<Transformation> = mutableListOf()

    init {
        val sourceSetContainer: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val transformations = sourceSetContainer.map { return@map SourceSetOutput(project, it) }
        sourceSetOutputs = transformations
    }

    fun register(transformation: IClassTransformer) {
        transformations.add(Transformation(buildDir, rootDestinationDir, outputFileCollections, transformation))
    }

    fun execute() {
        transformations.forEach { it.execute() }
    }
}