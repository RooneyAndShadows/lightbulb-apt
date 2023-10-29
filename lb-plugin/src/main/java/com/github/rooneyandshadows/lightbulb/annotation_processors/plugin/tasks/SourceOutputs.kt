package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Paths

class SourceOutputs(private val project: Project) {
    private val buildDir = project.buildDir.toString()
    private val destinationRoot = "transformations"
    val destinationsDir = project.file(Paths.get(buildDir, destinationRoot))
    val outputs: Map<String, String>
        get() {
            val outputs: MutableMap<String, String> = mutableMapOf()
            val sourceSetContainer: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
            sourceSetContainer.forEach { it ->
                outputs[it.name] = it.output.asPath
            }
            return outputs
        }
    val classFiles: List<File>
        get() {
            val classFiles: MutableList<File> = mutableListOf()
            outputs.forEach { (name, output) ->
                val outputFiles = output.asFileTree.filter { return@filter it.extension == "class" }.toList()
                classFiles.addAll(outputFiles)
            }
            return classFiles;
        }
    val destinations: Map<String, String>
        get() {
            val rootDir = "transformations"
            val destinations: MutableMap<String, String> = mutableMapOf()
            outputs.forEach { (name, output) ->
                val path = Paths.get(buildDir, rootDir, name).toString()
                destinations[name] = path
            }
            return destinations
        }

}