package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.transform

import com.android.build.api.variant.Variant
import com.github.rooneyandshadows.lightbulb.apt.plugin.*
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeParcelableSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeActivitySuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeApplicationSuperclassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.ChangeFragmentSuperclassTransformation
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
        transformationRegistry.register(ChangeApplicationSuperclassTransformation(packageNames,classNames))
        transformationRegistry.register(ChangeActivitySuperclassTransformation(packageNames,classNames))
        transformationRegistry.register(ChangeFragmentSuperclassTransformation(packageNames,classNames))
        transformationRegistry.register(ChangeParcelableSuperclassTransformation(packageNames,classNames))
    }


    @TaskAction
    fun taskAction() {
        val baseDir = output.get().asFile.parent
        val lightbulbDir = baseDir.appendDirectory("lightbulb")

        lightbulbDir.deleteDirectory()
        withJarOutputStream { jarOutputStream ->
            if (extension.isOutputEnabled()) {
                copySystemJars(jarOutputStream)
            }
            transformationRegistry.execute { classDir, className, byteCode ->
                if (extension.isOutputEnabled()) {
                    writeClassToJar(classDir, className, byteCode, jarOutputStream)
                }
                if (extension.isDumpEnabled()) {
                    dumpClassFile(lightbulbDir, classDir, className, byteCode)
                }
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

    private fun writeClassToJar(classDir: String, className: String, byteCode: ByteArray, jarOutput: JarOutputStream) {
        LoggingUtil.info("Adding to jar $className")
        val zipEntryName = classDir.plus("${className}.class")
        val entry = JarEntry(zipEntryName)
        jarOutput.putNextEntry(entry)
        jarOutput.write(byteCode)
        jarOutput.closeEntry()
    }

    private fun dumpClassFile(lightbulbDir: String, classDir: String, className: String, byteCode: ByteArray) {
        val dumpClassSuffix = "_Transformed"
        val dumpClassName = className.plus("${dumpClassSuffix}.class")
        val targetDir = lightbulbDir.appendDirectory(classDir)
        targetDir.createFile(dumpClassName).write(byteCode)
    }

    private fun getTransformationsClasspath(): FileCollection {
        val dirs = allDirectories.get().map {
            return@map it.asFile.path
        }
        return project.files(dirs)
    }

}