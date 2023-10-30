package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

class SourceSetOutput(
    private val project: Project,
    private val sourceSet: SourceSet,
) {
    val name: String = sourceSet.name
    val outputCollections: List<FileCollection>
    val classFiles: List<File>

    init {
        val targetOutputs = listOf(sourceSet.output.classesDirs, sourceSet.output.generatedSourcesDirs)
        outputCollections = targetOutputs.flatMap { it.files }.map { project.files(it) }
        classFiles = outputCollections.map {
            it.asFileTree.filter { file -> file.extension == "class" }.toList()
        }.flatten()
    }

    companion object {
        fun from(project: Project): List<SourceSetOutput> {
            return project.extensions.getByType(SourceSetContainer::class.java).map { SourceSetOutput(project, it) }
        }

        fun classFiles(vararg outputs: SourceSetOutput): List<File> {

        }
    }
}