package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.api.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformers.GroovyClassTransformation
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

@Suppress("MemberVisibilityCanBePrivate")
abstract class TransformationTask : DefaultTask() {
    private var destinationDir: Any = Paths.get(project.buildDir.toString(), "transformations", this.name).toFile()
    private var classesDir: Any? = null
    var transformation: IClassTransformer? = null

    @get:InputFiles
    val classpath: FileCollection = project.files()

    @get:InputFiles
    val sources: FileCollection
        get() {
            if (classesDir == null) {
                return project.files()
            }
            val result = project.fileTree(classesDir!!)
            result.include("**/*.class")
            return result
        }

    @OutputDirectory
    fun getDestinationDir(): File {
        return project.file(destinationDir)
    }

    @TaskAction
    fun exec() {
        val classPath: MutableCollection<File> = classpath.files
        if (classesDir != null) {
            classPath.add(project.file(classesDir!!))
        }
        val workDone = TransformationAction(
            getDestinationDir(),
            sources.files,
            classPath,
            transformation,
        ).execute()
        this.didWork = workDone
    }

    fun transform(transformClosure: Closure<Unit>, filterClosure: Closure<Boolean>?) {
        transformation = GroovyClassTransformation(transformClosure, filterClosure)
    }

    fun from(dir: Any?) {
        classesDir = dir
    }

    fun into(dir: Any) {
        destinationDir = dir
    }

    fun eachFile(closure: Closure<*>) {
        closure.call(sources.files)
    }
}