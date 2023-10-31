package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.common.SourceSetOutput
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Paths


@Suppress("unused")
abstract class TransformationsTask : DefaultTask() {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "transformations"
    private val rootDestinationDir: String = Paths.get(buildDir, destinationRoot).toString()
    private val sourceSetOutputs: List<SourceSetOutput> = SourceSetOutput.from(project)
    private val outputFileCollections: List<FileCollection> = SourceSetOutput.outputCollections(sourceSetOutputs)
    private val transformationRegistry = TransformationRegistry(buildDir, rootDestinationDir, outputFileCollections)

    @get:InputFiles
    val classFiles: List<File>
        get() = SourceSetOutput.classFiles(sourceSetOutputs)


    @get:OutputDirectory
    val destinationDir: File
        get() = project.file(rootDestinationDir)

    init {
        transformationRegistry.register(MyTransformation())
    }

    override fun getGroup(): String {
        return "lightbulb"
    }

    @TaskAction
    fun execute() {
        transformationRegistry.execute()
    }

    override fun doLast(action: Action<in Task>): Task {
        return super.doLast(action)
    }
}