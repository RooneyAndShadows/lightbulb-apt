package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.common.VariantOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject


@Suppress("unused")
abstract class TransformationsTask @Inject constructor(private val variantOutput: VariantOutput) : DefaultTask() {
    private val buildDir: String = project.buildDir.toString()
    private val destinationRoot: String = "transformations"
    private val rootDestinationDir: String = Paths.get(buildDir, destinationRoot, variantOutput.name).toString()
    private val transformationRegistry = TransformationRegistry(buildDir, rootDestinationDir, variantOutput)

    @get:Classpath
    val classPath: FileCollection
        get() = variantOutput.globalClassPath

    @get:InputFiles
    val classFiles: List<File>
        get() = variantOutput.transformationClassFiles

    @get:OutputDirectory
    val destinationDir: File
        get() = project.file(rootDestinationDir)

    init {
        transformationRegistry.register(MyTransformation())
    }

    @Override
    override fun getGroup(): String {
        return "lightbulb"
    }

    @TaskAction
    fun execute() {
        transformationRegistry.execute()
        val source = Paths.get(rootDestinationDir).toFile()
        val target = Paths.get(buildDir).toFile()
        copyFolder(source, target)
        deleteDirectory(source)
    }

    private fun copyFolder(src: File, dest: File) {
        if (!src.isDirectory) return
        if (dest.exists()) {
            if (!dest.isDirectory) {
                //System.out.println("destination not a folder " + dest);
                return
            }
        } else {
            dest.mkdir()
        }
        val files = src.listFiles()
        if (files == null || files.isEmpty()) return
        for (file in files) {
            val fileDest = File(dest, file.name)
            //System.out.println(fileDest.getAbsolutePath());
            if (file.isDirectory) {
                copyFolder(file, fileDest)
            } else {
                try {
                    Files.copy(file.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private fun deleteDirectory(directoryToBeDeleted: File): Boolean {
        val allContents = directoryToBeDeleted.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDirectory(file)
            }
        }
        return directoryToBeDeleted.delete()
    }
}