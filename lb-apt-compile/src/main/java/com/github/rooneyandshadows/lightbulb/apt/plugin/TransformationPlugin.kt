package com.github.rooneyandshadows.lightbulb.apt.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.extension.impl.VariantSelectorImpl
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ProcessorOptionNames
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil
import com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.TransformationsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

@Suppress("unused", "UNUSED_VARIABLE")
class TransformationPlugin : Plugin<Project> {
    private var configured = false
    override fun apply(project: Project) {
        this.configure(project)

        if (configured) {
            val extension = project.extensions.create(PLUGIN_EXTENSION_NAME, TransformExtension::class.java)
            setupLogger(extension)
            configureTransformationTask(project, extension)
        }
    }

    private fun setupLogger(extension: TransformExtension) {
        LoggingUtil.enabled = extension.debug
    }

    private fun configureAPT(extension: TransformExtension, variant: Variant) {
        val rootPackageArg = ProcessorOptionNames.PROJECT_ROOT_PACKAGE
        val rootPackageValue = extension.projectRootPackage
        variant.addAnnotationProcessorArgument(rootPackageArg, rootPackageValue)
    }

    private fun configureTransformationTask(project: Project, extension: TransformExtension) {
        val ext = project.androidComponents()

        ext.onVariants(VariantSelectorImpl().withName("debug")) { variant ->
            configureAPT(extension, variant)
            val capitalizedVariantName = variant.name.capitalized()
            val taskName = "transform${capitalizedVariantName}"
            val taskType = TransformationsTask::class.java
            val taskProvider = project.tasks.register<TransformationsTask>(taskName, variant)
            variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    { it.allJars },
                    TransformationsTask::allDirectories,
                    TransformationsTask::output
                )
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
            project.dependencies.add("kapt", LIGHTBULB_APT_PROCESSOR_DEPENDENCY_NOTATION)
        } else {
            project.dependencies.add("annotationProcessor", LIGHTBULB_APT_PROCESSOR_DEPENDENCY_NOTATION)
        }

        project.dependencies.add("implementation", LIGHTBULB_APT_PROCESSOR_DEPENDENCY_NOTATION)
        project.dependencies.add("implementation", LIGHTBULB_APT_CORE_DEPENDENCY_NOTATION)
        configured = true
    }
}