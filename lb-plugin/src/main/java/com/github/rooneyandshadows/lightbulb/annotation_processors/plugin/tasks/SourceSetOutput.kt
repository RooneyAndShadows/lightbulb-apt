package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import java.io.File

class SourceSetOutput(
    private val project: Project,
    private val sourceSet: SourceSet,
) {
    val outputCollections: List<FileCollection>
        get() {
            val mergedFileCollection = listOf(sourceSet.output.classesDirs, sourceSet.output.generatedSourcesDirs)
            val outputFileCollections: MutableList<FileCollection> = mutableListOf()
            mergedFileCollection.forEach { fileCollection ->
                fileCollection.forEach { dir ->
                    outputFileCollections.add(project.files(dir))
                }
            }
            return outputFileCollections
        }
    val classFiles: List<File>
        get() = outputCollections.map {
            it.asFileTree.filter { file -> file.extension == "class" }.toList()
        }.flatten()
}