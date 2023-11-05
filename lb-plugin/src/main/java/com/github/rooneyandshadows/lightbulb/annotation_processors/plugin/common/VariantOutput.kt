package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

@Suppress("DEPRECATION")
class VariantOutput(val variant: BaseVariant, bootClassPath: List<File>) {
    val name: String = variant.name
    val classPath: FileCollection
    val classFiles: List<File>

    init {
        classPath = variant.javaCompileProvider.get().classpath
        classPath.plus(bootClassPath)
        classFiles = extractClassFiles()
    }

    private fun extractClassFiles(): List<File> {
        return classPath.asFileTree.filter { file -> file.extension == "class" }.toList()
    }

    companion object {
        fun from(project: Project): List<VariantOutput> {
            val androidExtension = project.baseExtension() ?: error("Android BaseExtension not found.")
            val variantOutputs = mutableListOf<VariantOutput>()
            val bootClassPath = androidExtension.bootClasspath
            androidExtension.forEachRootVariant { variant ->
                variantOutputs.add(VariantOutput(variant, bootClassPath))
            }
            return variantOutputs
        }

        fun classFiles(outputs: List<VariantOutput>): List<File> {
            return outputs.map { output -> output.classFiles }.flatten()
        }

        fun classPath(outputs: List<VariantOutput>): List<FileCollection> {
            return outputs.map { return@map it.classPath }
        }

        private fun BaseExtension.forEachRootVariant(
            @Suppress("DEPRECATION") block: (variant: BaseVariant) -> Unit
        ) {
            when (this) {
                is AppExtension -> {
                    // For an app project we configure the app variant and both androidTest and unitTest
                    // variants, Hilt components are generated in all of them.
                    applicationVariants.all { block(this) }
                    testVariants.all { block(this) }
                    unitTestVariants.all { block(this) }
                }

                is LibraryExtension -> {
                    // For a library project, only the androidTest and unitTest variant are configured since
                    // Hilt components are not generated in a library.
                    testVariants.all { block(this) }
                    unitTestVariants.all { block(this) }
                }

                is TestExtension -> {
                    applicationVariants.all { block(this) }
                }

                else -> error("Hilt plugin does not know how to configure '$this'")
            }
        }

        private fun Project.baseExtension(): BaseExtension? = extensions.findByType(BaseExtension::class.java)
    }
}