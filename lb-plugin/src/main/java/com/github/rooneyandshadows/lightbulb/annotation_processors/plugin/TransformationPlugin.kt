package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.common.VariantOutput
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation.TransformationsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

@Suppress("unused", "UNUSED_VARIABLE")
class TransformationPlugin : Plugin<Project> {
    private val kaptPluginId: String = "org.jetbrains.kotlin.kapt"
    private val kotlinAndroidPluginId: String = "org.jetbrains.kotlin.android"
    private var configured = false

    override fun apply(project: Project) {
        this.configure(project)
        if (configured) {
            project.afterEvaluate {
                val variantsOutput = VariantOutput.from(project)
                variantsOutput.forEach { variantOutput ->
                    val capitalizedVariantName = variantOutput.name.capitalized()
                    val taskName = "transform${capitalizedVariantName}"
                    val taskType = TransformationsTask::class.java
                    val transformationsTask = project.tasks.register(taskName, taskType, variantOutput).get()
                    transformationsTask.apply {
                        // Built by added for task dependency between the output and the task
                        val taskOutput = project.files(destinationDir).builtBy(this)
                        // This registers the transformation task in the Android pipeline as something that generates
                        // byte code. This is technically designed to _add_ things to the pipeline.
                        // Since we've replaced the actual classes with the transformed ones and
                        // removed the java/kotlin output transformations, it's only purpose is to schedule the transformation task.
                        variantOutput.variant.registerPostJavacGeneratedBytecode(taskOutput)
                    }
                }
            }
        }
    }


    private fun configure(project: Project) {
        if (configured) return
        val androidBasePlugin = project.plugins.withType(AndroidBasePlugin::class.java)
        val appPlugin = project.plugins.withType(AppPlugin::class.java)
        val libPlugin = project.plugins.withType(LibraryPlugin::class.java)
        val hasKaptApplied = project.plugins.hasPlugin(kaptPluginId)
        val isKotlinProject = project.plugins.hasPlugin(kotlinAndroidPluginId)
        val isAndroidProject = androidBasePlugin.isNotEmpty()

        if (!isAndroidProject) {
            throw IllegalStateException("lb-compile Gradle plugin should be only applied to an Android projects.")
        }

        val dependencyNotation = "com.github.rooneyandshadows.lightbulb-annotation-processors:lb-processor:%s"
        if (isKotlinProject) {
            if (!hasKaptApplied) {
                project.plugins.apply(kaptPluginId)
            }
            project.dependencies.add("kapt", dependencyNotation.format("1.1.0"))
        } else {
            project.dependencies.add("annotationProcessor", dependencyNotation.format("1.1.0"))
        }
        project.dependencies.add("implementation", dependencyNotation.format("1.1.0"))
        configured = true
    }
}