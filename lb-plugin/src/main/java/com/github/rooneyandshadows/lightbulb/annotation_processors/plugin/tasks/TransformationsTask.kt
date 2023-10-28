package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths


@Suppress("unused")
abstract class TransformationsTask : DefaultTask() {
    private val transformations: Transformations
    private val classesDir: FileCollection
        get() {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            return sourceSets.asMap["main"]!!.output
        }

    @get:InputFiles
    val classFiles: FileCollection
        get() {
            return classesDir.asFileTree.filter { return@filter it.extension == "class" }
        }

    @get:OutputDirectory
    val destinationDir: File
        get() {
            val buildDir = project.buildDir.toString()
            return project.file(Paths.get(buildDir, "transformations").toFile())
        }


    init {
        transformations = Transformations(project, classesDir, destinationDir).apply {
            register(MyTransformation())
        }
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