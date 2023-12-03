package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.globalClasspathForVariant
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.common.TransformationJobRegistry
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeFragmentSuperclassTransformation

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

abstract class TransformationsTask @Inject constructor(private val variant: Variant) : DefaultTask() {
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
        transformationRegistry = TransformationJobRegistry(globalClasspath) { getTransformationsClasspath() }
        transformationRegistry.register(ChangeFragmentSuperclassTransformation())
        transformationRegistry.register(ChangeActivitySuperclassTransformation())
    }

    @TaskAction
    fun taskAction() {
        val jars = allJars.get()

        if (jars.size <= 0) {
            return
        }

        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )
        jarOutput.use {
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
            transformationRegistry.execute(jarOutput)
        }
    }


    private fun getTransformationsClasspath(): FileCollection {
        val dirs = allDirectories.get().map {
            return@map it.asFile.path
        }
        return project.files(dirs)
    }

}