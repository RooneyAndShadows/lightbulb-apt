package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.*
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.job.TransformationJobRegistry
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeApplicationSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeFragmentSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeParcelableSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames
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
        val globalClassPath = project.globalClasspathForVariant(variant)

        transformationRegistry = TransformationJobRegistry(globalClassPath) { getTransformationsClasspath() }
        transformationRegistry.register(ChangeApplicationSuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeActivitySuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeFragmentSuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeParcelableSuperclassTransformation(packageNames))
    }

    override fun getGroup(): String {
        return PLUGIN_TASK_GROUP
    }

    @TaskAction
    fun taskAction() {
        val baseDir = project.buildDir.path
        val lightbulbDir = baseDir.appendDirectory("intermediates", "lightbulb")

        lightbulbDir.deleteDirectory()

        LoggingUtil.info("Classes dump directory: $lightbulbDir")
        transformationRegistry.execute { classDir, className, modified, byteCode ->
            if (modified) {
                val dumpClassSuffix = "_Transformed"
                val dumpClassName = className.plus(".class")
                val targetDir = lightbulbDir.appendDirectory(classDir)
                val targetFile = targetDir.createFile(dumpClassName)

                LoggingUtil.info("Dumping class \"${className}\" into: ${targetFile.path}")
                targetFile.write(byteCode)
            }
        }
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
}