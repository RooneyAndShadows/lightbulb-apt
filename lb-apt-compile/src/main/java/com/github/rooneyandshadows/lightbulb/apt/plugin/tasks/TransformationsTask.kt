package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.*
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.job.TransformationJobRegistry
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeParcelableSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeApplicationSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeFragmentSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.util.jar.JarFile
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.inject.Inject

abstract class TransformationsTask @Inject constructor(
    private val variant: Variant,
    private val extension: TransformExtension
) : DefaultTask() {
    private val transformationRegistry: TransformationJobRegistry

    @get:InputFiles
    val globalClasspath: FileCollection
        get() = project.globalClasspathForVariant(variant)

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    init {
        val packageNames = PackageNames(project.androidNamespace())
        val classNames = ClassNames(packageNames)

        transformationRegistry = TransformationJobRegistry(globalClasspath) { getTransformationsClasspath() }
        transformationRegistry.register(ChangeApplicationSuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeActivitySuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeFragmentSuperclassTransformation(packageNames, classNames))
        transformationRegistry.register(ChangeParcelableSuperclassTransformation(packageNames, classNames))
    }

    @Override
    override fun getGroup(): String {
        return PLUGIN_TASK_GROUP
    }

    @TaskAction
    fun taskAction() {
        withJarOutputStream { jarOutputStream ->
            copySystemJars(jarOutputStream)
            transformationRegistry.execute { classDir, className, modified, byteCode ->
                LoggingUtil.info("Adding to jar $className")
                val zipEntryName = classDir.plus("${className}.class")
                val entry = JarEntry(zipEntryName)
                jarOutputStream.putNextEntry(entry)
                jarOutputStream.write(byteCode)
                jarOutputStream.closeEntry()
            }
        }
    }

    private fun withJarOutputStream(action: (jarOutputStream: JarOutputStream) -> Unit) {
        val outputAsFile = output.get().asFile
        JarOutputStream(BufferedOutputStream(FileOutputStream(outputAsFile))).use(action)
    }

    private fun copySystemJars(jarOutput: JarOutputStream) {
        val jars = allJars.get()
        jars.forEach { file ->
            //info("handling " + file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                //info("Adding from jar ${jarEntry.name}")
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
            jarFile.close()
        }
    }

    private fun getTransformationsClasspath(): FileCollection {
        val dirs = allDirectories.get().map {
            return@map it.asFile.path
        }
        return project.files(dirs)
    }
}