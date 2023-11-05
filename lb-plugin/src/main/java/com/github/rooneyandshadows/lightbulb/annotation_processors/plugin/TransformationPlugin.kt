package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.*
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation.TransformationsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import java.nio.file.Files

@Suppress("unused", "UNUSED_VARIABLE")
class TransformationPlugin : Plugin<Project> {
    private val kaptPluginId: String = "org.jetbrains.kotlin.kapt"
    private val kotlinAndroidPluginId: String = "org.jetbrains.kotlin.android"
    private var configured = false

    override fun apply(project: Project) {
        this.configure(project)
        if (configured) {
            val transformationsTask = project.tasks.register(
                "transformations",
                TransformationsTask::class.java
            ) {
                dependsOn("compileJava")
            }.get()
            project.tasks.getByName("classes") {
                dependsOn("transformations")
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
            //throw IllegalStateException("lb-compile Gradle plugin should be only applied to an Android projects.")
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