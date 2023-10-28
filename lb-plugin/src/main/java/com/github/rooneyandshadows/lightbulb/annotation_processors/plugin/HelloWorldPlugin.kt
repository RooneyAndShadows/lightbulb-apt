package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create

class HelloWorldPlugin : Plugin<Project> {
    private val kaptPluginId: String = "org.jetbrains.kotlin.kapt"
    private val kotlinAndroidPluginId: String = "org.jetbrains.kotlin.android"
    private var configured = false

    override fun apply(project: Project) {
        this.configure(project)

        if (configured) {
            val extension = project.extensions.create<HelloWorldExtension>("greeting")
            val task = project.tasks.register("hello", SayHelloTask::class.java, extension)
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val from = sourceSets.asMap["main"]!!.output

            project.tasks.getByName("compileJava").doLast {
                val transformation = Transformation(project, MyTransformation(), from)
                transformation.execute()
                // task1.get().exec()
            }
            project.tasks.getByName("compileJava").doLast {
                task.get().greet()
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
        println(dependencyNotation)
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