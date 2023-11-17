package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.PLUGIN_TASK_GROUP
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.VariantOutput
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.copyFolder
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.deleteDirectory
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.common.TransformationJobRegistry
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject


@Suppress("unused")
abstract class TransformationsTask @Inject constructor(private val variantOutput: VariantOutput) : DefaultTask() {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "intermediates/lightbulb/classes"
    private val rootDestinationDir: String = Paths.get(buildDir, destinationRoot, variantOutput.name).toString()
    //private val transformationRegistry: TransformationJobRegistry

    @get:InputFiles
    @get:Classpath
    //This makes the task to start after all the tasks that have outputs contained in the classpath
    val globalClassPath: FileCollection
        get() = variantOutput.globalClassPath

    @get:InputFiles
    val transformationsClassPath: FileCollection
        get() = variantOutput.transformationsClassPath

    //@get:OutputDirectories
    //val transformedFiles: FileCollection
    //    get() = variantOutput.transformationsClassPath

    @get:OutputDirectory
    val destinationDir: File
        get() = project.file(rootDestinationDir)

    init {
        //transformationRegistry = TransformationJobRegistry(
        //    buildDir,
        //    rootDestinationDir,
        //    globalClassPath,
        //    transformationsClassPath
        //)
        //transformationRegistry.register(MyTransformation())
    }

    @Override
    override fun getGroup(): String {
        return PLUGIN_TASK_GROUP
    }

    @TaskAction
    fun execute() {
        //transformationRegistry.execute()
        val source = Paths.get(rootDestinationDir).toFile()
        val target = Paths.get(buildDir).toFile()
        //source.copyFolder(target)
       // source.deleteDirectory()
    }
}