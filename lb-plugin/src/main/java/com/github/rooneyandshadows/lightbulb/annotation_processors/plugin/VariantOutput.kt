@file:Suppress("DEPRECATION")

package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

@Suppress("DEPRECATION")
class VariantOutput(
    private val project: Project,
    private val bootClassPath: List<File>,
    val variant: BaseVariant,
) {
    val name: String = variant.name
    val globalClassPath: FileCollection
    val transformationsClassPath: FileCollection
    val transformationClassFiles: List<File>

    init {
        val capitalizedVariantName = name.capitalized()
        val ktCompileTaskForVariant = project.tasks.findByName("compile${capitalizedVariantName}Kotlin")
        globalClassPath = createClassPoolClassPath(ktCompileTaskForVariant)
        transformationsClassPath = createTransformationsClassPath(ktCompileTaskForVariant)
        transformationClassFiles = extractClassFiles(transformationsClassPath)
    }

    private fun extractClassFiles(classPath: FileCollection): List<File> {
        return classPath.asFileTree.filter { file -> file.extension == "class" }.toList()
    }

    private fun createClassPoolClassPath(kotlinCompileTask: Task?): FileCollection {
        val javaCompileClassPath = variant.javaCompileProvider.get().outputs.files
        val ktCompileClassPath = kotlinCompileTask?.outputs?.files
        var classPath: FileCollection = project.files(bootClassPath)
        classPath = classPath.plus(variant.javaCompileProvider.get().classpath)
        classPath = classPath.plus(javaCompileClassPath)
        ktCompileClassPath?.apply {
            classPath = classPath.plus(ktCompileClassPath)
        }
        return classPath
    }

    private fun createTransformationsClassPath(kotlinCompileTask: Task?): FileCollection {
        val javaCompileClassPath = variant.javaCompileProvider.get().outputs.files
        val ktCompileClassPath = kotlinCompileTask?.outputs?.files
        var classPath: FileCollection = project.files()
        classPath = classPath.plus(javaCompileClassPath)
        ktCompileClassPath?.apply {
            classPath = classPath.plus(ktCompileClassPath)
        }
        return classPath
    }


    companion object {
        fun from(project: Project): List<VariantOutput> {
            val androidExtension = project.baseExtension() ?: error("Android BaseExtension not found.")
            val variantOutputs = mutableListOf<VariantOutput>()
            val bootClassPath = androidExtension.bootClasspath
            androidExtension.forEachRootVariant { variant ->
                variantOutputs.add(VariantOutput(project, bootClassPath, variant))
            }
            return variantOutputs
        }
    }
}