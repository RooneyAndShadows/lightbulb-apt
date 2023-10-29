package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import org.gradle.api.tasks.SourceSet
import java.io.File
import java.nio.file.Paths

class SourceSetTransformation(private val sourceSet: SourceSet, private val rootDestination: String) {
    val name: String = sourceSet.name
    val classesDir: String = sourceSet.output.asPath
    val resourcesDir: String = sourceSet.resources.asPath
    val destinationDir: String = Paths.get(rootDestination, sourceSet.name).toString()
    val classFiles: List<File> = sourceSet.output.files.filter { return@filter it.extension == "class" }
    val resourceFiles: List<File> = sourceSet.resources.files.toList()
}