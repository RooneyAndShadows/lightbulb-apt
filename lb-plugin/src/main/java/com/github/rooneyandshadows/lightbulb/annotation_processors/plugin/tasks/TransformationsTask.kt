package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.*
import java.io.File


@Suppress("unused")
abstract class TransformationsTask : DefaultTask() {
    private val transformations = Transformations(project)

    @get:InputFiles
    val classFiles: List<File>
        get() = transformations.getTransformationClassFiles()


    @get:OutputDirectory
    val destinationDir: File
        get() = transformations.getRootDestinationDir()

    init {
        transformations.register(MyTransformation())
    }

    override fun getGroup(): String {
        return "lightbulb"
    }

    @TaskAction
    fun execute() {
        transformations.execute()
    }

    override fun doLast(action: Action<in Task>): Task {
        return super.doLast(action)
    }
}