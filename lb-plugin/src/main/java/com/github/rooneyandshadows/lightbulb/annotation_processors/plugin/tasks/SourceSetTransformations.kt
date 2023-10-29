package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Paths

class SourceSetTransformations(project: Project) {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "transformations"
    val rootDestinationDir: String = Paths.get(buildDir, destinationRoot).toString()
    val sourceSetTransformations: List<SourceSetTransformation>
    val classFiles: List<File>
        get() = sourceSetTransformations.map { return@map it.classFiles }.flatten()
    val resourceFiles: List<File>
        get() = sourceSetTransformations.map { return@map it.resourceFiles }.flatten()


    init {
        val sourceSetContainer: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val transformations = sourceSetContainer.map { return@map SourceSetTransformation(it, rootDestinationDir) }
        sourceSetTransformations = transformations
    }
}