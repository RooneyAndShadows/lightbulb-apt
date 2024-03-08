package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.*
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.job.TransformationJobRegistry
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeParcelableSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeApplicationSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.ChangeFragmentSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames

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

        transformationRegistry = TransformationJobRegistry(globalClasspath) { getTransformationsClasspath() }
        transformationRegistry.register(ChangeApplicationSuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeActivitySuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeFragmentSuperclassTransformation(packageNames))
        transformationRegistry.register(ChangeParcelableSuperclassTransformation(packageNames))
    }

    @Override
    override fun getGroup(): String {
        return PLUGIN_TASK_GROUP
    }

    @TaskAction
    fun taskAction() {
        getTransformationsClasspath().asFileTree.forEachIndexed { index, file ->
            println("%d: %s".format(index,file.path))
        }

        val baseDir = project.buildDir.path
        val lightbulbDir = baseDir.appendDirectory("intermediates", "lightbulb")

        LoggingUtil.info("Classes dump directory: $lightbulbDir")

        withJarOutputStream { jarOutputStream ->
            copySystemJars(jarOutputStream)
            transformationRegistry.execute { classDir, className, modified, byteCode ->
                writeIntoJar(jarOutputStream, classDir, className, byteCode)
                if (modified) {
                    dumpClass(lightbulbDir, classDir, className, byteCode)
                }
            }
        }
    }

    private fun dumpClass(
        dumpDir: String,
        classDir: String,
        className: String,
        content: ByteArray
    ) {
        val dumpClassName = className.plus(".class")
        val targetDir = dumpDir.appendDirectory(classDir)
        val targetFile = targetDir.createFile(dumpClassName)

        targetFile.write(content)
    }

    private fun writeIntoJar(
        jarOutputStream: JarOutputStream,
        classDir: String,
        className: String,
        content: ByteArray
    ) {
        LoggingUtil.info("Adding to jar $className")
        val zipEntryName = classDir.plus("${className}.class")
        val entry = JarEntry(zipEntryName)
        jarOutputStream.putNextEntry(entry)
        jarOutputStream.write(content)
        jarOutputStream.closeEntry()
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