package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger.LoggingUtil
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.TransformationsTask
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ProcessorOptionNames
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized

@Suppress("unused", "UNUSED_VARIABLE")
class TransformationPlugin : Plugin<Project> {
    private var configured = false

    override fun apply(project: Project) {
        this.configure(project)

        if (configured) {
            val extension = project.extensions.create(PLUGIN_EXTENSION_NAME, TransformExtension::class.java)
            project.afterEvaluate {
                setupLogger(extension)
                configureAPT(project, extension)
                configureTransformationTask(project)
            }
        }
    }

    private fun setupLogger(extension: TransformExtension) {
        LoggingUtil.enabled = extension.debug
    }

    private fun configureAPT(project: Project, extension: TransformExtension) {
        project.tasks.withType(JavaCompile::class.java).all {
            val rootPackageArg = ProcessorOptionNames.PROJECT_ROOT_PACKAGE
            val rootPackageValue = extension.projectRootPackage
            options.addAnnotationProcessorArgument(rootPackageArg, rootPackageValue)
        }
    }

    private fun configureTransformationTask(project: Project) {
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

    private fun configure(project: Project) {
        if (configured) return
        val androidBasePlugin = project.plugins.withType(AndroidBasePlugin::class.java)
        val appPlugin = project.plugins.withType(AppPlugin::class.java)
        val libPlugin = project.plugins.withType(LibraryPlugin::class.java)
        val hasKaptApplied = project.plugins.hasPlugin(KAPT_PLUGIN_ID)
        val isKotlinProject = project.plugins.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID)
        val isAndroidProject = androidBasePlugin.isNotEmpty()

        if (!isAndroidProject) {
            throw IllegalStateException("lb-compile Gradle plugin should be only applied to an Android projects.")
        }

        if (isKotlinProject) {
            if (!hasKaptApplied) {
                project.plugins.apply(KAPT_PLUGIN_ID)
            }
            //project.dependencies.add("kapt", LIGHTBULB_APT_DEPENDENCY_NOTATION)
        } else {
            //project.dependencies.add("annotationProcessor", LIGHTBULB_APT_DEPENDENCY_NOTATION)
        }

        project.dependencies.add("implementation", LIGHTBULB_APT_DEPENDENCY_NOTATION)
        configured = true
    }
}