@file:Suppress("SpellCheckingInspection", "DEPRECATION", "UnstableApiUsage", "UNCHECKED_CAST")

package com.github.rooneyandshadows.lightbulb.apt.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File
import java.io.FileOutputStream

private const val rootProject = "com.github.rooneyandshadows.lightbulb-apt"
private const val version = "2.0.0-RC4"

const val PLUGIN_EXTENSION_NAME = "lightbulb"
const val PLUGIN_TASK_GROUP = "lightbulb"
const val KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt"
const val KOTLIN_ANDROID_PLUGIN_ID = "org.jetbrains.kotlin.android"
const val LIGHTBULB_APT_PROCESSOR_DEPENDENCY_NOTATION = "${rootProject}:lb-apt-processor:${version}"
const val LIGHTBULB_APT_CORE_DEPENDENCY_NOTATION = "${rootProject}:lb-apt-core:${version}"
const val LIGHTBULB_APT_ANNOTATIONS_DEPENDENCY_NOTATION = "${rootProject}:lb-apt-annotations:${version}"

fun Variant.addAnnotationProcessorArgument(name: String, value: String) {
    javaCompilation.annotationProcessor.arguments.put(name, value)
}

fun Project.androidComponents(): ApplicationAndroidComponentsExtension {
    return project.extensions.getByName("androidComponents") as ApplicationAndroidComponentsExtension
}

fun Project.androidNamespace(): String {
    return baseExtension()!!.namespace!!
}

fun Project.baseExtension(): BaseExtension? {
    return extensions.findByType(BaseExtension::class.java)
}

fun Project.bootClasspath(): FileCollection {
    val bootClasspath = baseExtension()?.bootClasspath
    return files(bootClasspath)
}

fun Project.globalClasspathForVariant(variant: Variant): FileCollection {
    val variantClasspath = variant.compileClasspath
    var globalClasspath = bootClasspath()
    globalClasspath = globalClasspath.plus(variantClasspath)
    return globalClasspath
}

fun File.write(byteArray: ByteArray) {
    FileOutputStream(this).use { outputStream -> outputStream.write(byteArray) }
}

fun String.deleteDirectory(): Boolean {
    val directory = File(this)
    val allContents = directory.listFiles()
    if (allContents != null) {
        for (file in allContents) {
            file.path.deleteDirectory()
        }
    }
    return directory.delete()
}

fun String.createFile(filename: String): File {
    val dir = File(this)
    if (!dir.exists()) dir.mkdirs()
    return File("$this/$filename")
}

fun String.appendDirectory(vararg dirs: String): String {
    if (dirs.isEmpty()) {
        return this
    }

    var result = this.trimEnd('\\', '/')

    dirs.forEachIndexed { _, directory ->
        val trimmed = directory.trimEnd('\\', '/')

        result = result.plus("/${trimmed}")

    }

    return result
}
