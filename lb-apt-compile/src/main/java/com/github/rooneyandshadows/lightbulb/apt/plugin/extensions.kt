@file:Suppress("SpellCheckingInspection", "DEPRECATION", "UnstableApiUsage", "UNCHECKED_CAST")

package com.github.rooneyandshadows.lightbulb.apt.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import javassist.CtClass
import javassist.bytecode.annotation.AnnotationImpl
import javassist.bytecode.annotation.Annotation
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File
import java.io.IOException
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.nio.file.StandardCopyOption

const val PLUGIN_EXTENSION_NAME = "lightbulb"
const val PLUGIN_TASK_GROUP = "lightbulb"
const val KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt"
const val KOTLIN_ANDROID_PLUGIN_ID = "org.jetbrains.kotlin.android"
const val LIGHTBULB_APT_PROCESSOR_DEPENDENCY_NOTATION = "com.github.rooneyandshadows.lightbulb-apt:lb-apt-processor:2.0.0"
const val LIGHTBULB_APT_CORE_DEPENDENCY_NOTATION = "com.github.rooneyandshadows.lightbulb-apt:lb-apt-core:2.0.0"

fun Variant.addAnnotationProcessorArgument(name: String, value: String) {
    javaCompilation.annotationProcessor.arguments.put(name, value)
}

fun Project.androidComponents(): ApplicationAndroidComponentsExtension {
    return project.extensions.getByName("androidComponents") as ApplicationAndroidComponentsExtension
}

fun Project.baseExtension(): BaseExtension? {
    return extensions.findByType(BaseExtension::class.java)
}

fun Project.bootClasspath(): FileCollection {
    val bootClasspath = baseExtension()?.bootClasspath
    return files(bootClasspath)
}

fun <T : kotlin.Annotation> CtClass.getAnnotationHandler(annotationClass: Class<T>): Annotation? {
    val proxiedAnnotation: T = getAnnotation(annotationClass) as T? ?: return null
    if (!Proxy.isProxyClass(proxiedAnnotation.javaClass)) {
        return null
    }
    val impl = Proxy.getInvocationHandler(proxiedAnnotation) as AnnotationImpl
    return impl.annotation
}

fun Project.globalClasspathForVariant(variant: Variant): FileCollection {
    val variantClasspath = variant.compileClasspath
    var globalClasspath = bootClasspath()
    globalClasspath = globalClasspath.plus(variantClasspath)
    return globalClasspath
}

fun BaseExtension.forEachRootVariant(@Suppress("DEPRECATION") block: (variant: BaseVariant) -> Unit) {
    when (this) {
        is AppExtension -> {
            applicationVariants.forEach(block)
            testVariants.forEach(block)
            unitTestVariants.forEach(block)
        }

        is LibraryExtension -> {
            testVariants.forEach(block)
            unitTestVariants.forEach(block)
        }

        is TestExtension -> {
            applicationVariants.forEach(block)
        }

        else -> error("lb-compile plugin does not know how to configure '$this'")
    }
}

fun File.copyFolder(dest: File) {
    if (!isDirectory) return
    if (dest.exists()) {
        if (!dest.isDirectory) return
    } else {
        dest.mkdir()
    }
    val files = listFiles()
    if (files == null || files.isEmpty()) return
    for (file in files) {
        val fileDest = File(dest, file.name)
        //System.out.println(fileDest.getAbsolutePath());
        if (file.isDirectory) {
            file.copyFolder(fileDest)
        } else {
            try {
                Files.copy(file.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                //e.printStackTrace();
            }
        }
    }
}

fun File.deleteDirectory(): Boolean {
    val allContents = listFiles()
    if (allContents != null) {
        for (file in allContents) {
            file.deleteDirectory()
        }
    }
    return this.delete()
}

