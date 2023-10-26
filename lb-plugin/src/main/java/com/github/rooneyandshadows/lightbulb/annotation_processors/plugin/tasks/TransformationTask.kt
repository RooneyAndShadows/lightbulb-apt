package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.api.IClassTransformer
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformers.GroovyClassTransformation
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

@Suppress("MemberVisibilityCanBePrivate")
abstract class TransformationTask : DefaultTask() {
    private var destinationDir: Any = Paths.get(project.buildDir.toString(), "transformations", this.name).toFile()
    private var classesDir: FileCollection = project.files()

    private val classpath: List<File>
        get() = classesDir.map {
            return@map project.file(it)
        }
    private val classFiles: FileCollection
        get() {
            return classesDir.asFileTree.filter {
                return@filter it.extension == "class"
            }
        }

    @get:Input
    var transformation: IClassTransformer? = null

    @OutputDirectory
    fun getDestinationDir(): File {
        return project.file(destinationDir)
    }

    @TaskAction
    fun exec() {

        println("classesDir===========================")
        classesDir.forEach {
            println(it.path)
        }
        println("classpath===========================")
        classpath.forEach {
            println(it.path)
        }
        println("sources===========================")
        classFiles.forEach {
            println(it.path)
        }

        val workDone = TransformationAction(
            getDestinationDir(),
            classFiles.files,
            classpath,
            transformation,
        ).execute()
        this.didWork = workDone
    }

    fun transform(transformClosure: Closure<Unit>, filterClosure: Closure<Boolean>?) {
        transformation = GroovyClassTransformation(transformClosure, filterClosure)
    }

    fun from(dir: FileCollection) {
        classesDir = dir
    }

    fun into(dir: Any) {
        destinationDir = dir
    }

    fun eachFile(closure: Closure<*>) {
        closure.call(classFiles.files)
    }
}