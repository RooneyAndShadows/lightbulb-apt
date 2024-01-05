package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.transform

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.*
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeApplicationSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeFragmentSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeParcelableSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import javax.inject.Inject

abstract class TransformationsTaskDump @Inject constructor(
    private val variant: Variant,
    private val extension: TransformExtension
) : DefaultTask() {
    private val transformationRegistry: TransformationJobRegistry

    init {
        val packageNames = PackageNames(project.androidNamespace())
        val classNames = ClassNames(packageNames)
        val globalClassPath = project.globalClasspathForVariant(variant)

        transformationRegistry = TransformationJobRegistry(globalClassPath) { getTransformationsClasspath() }
        transformationRegistry.register(ChangeApplicationSuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeActivitySuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeFragmentSuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeParcelableSuperclassTransformation(packageNames, classNames))
    }

    private fun getTransformationsClasspath(): FileCollection {
        val javaCompileTask = project.tasks.findByName("compile${variant.name.capitalized()}JavaWithJavac")
        val ktCompileTaskForVariant = project.tasks.findByName("compile${variant.name.capitalized()}Kotlin")
        var classpath: FileCollection = project.files()
        javaCompileTask?.apply {
            classpath = classpath.plus(outputs.files)
        }
        ktCompileTaskForVariant?.apply {
            classpath = classpath.plus(outputs.files)
        }
        return classpath
    }

    @TaskAction
    fun taskAction() {
        val baseDir = project.buildDir.path
        val lightbulbDir = baseDir.appendDirectory("lightbulb")
        transformationRegistry.execute { classDir, className, byteCode ->
            val dumpClassSuffix = "_Transformed"
            val dumpClassName = className.plus("${dumpClassSuffix}.class")
            val targetDir = lightbulbDir.appendDirectory(classDir)
            targetDir.createFile(dumpClassName).write(byteCode)
        }
    }
}