package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger.LoggingUtil.Companion.severe
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger.LoggingUtil.Companion.warning
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.base.IClassTransformer
import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import java.io.*
import java.nio.file.Paths

@Suppress("MemberVisibilityCanBePrivate")
class TransformationJob(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: FileCollection,
    private val transformation: IClassTransformer
) {
    fun execute(): Boolean {
        info("Executing transformations: ${transformation.javaClass.name}")

        try {
            val classPool = setupClassPool()
            val loadedClasses = preloadClasses(classPool)

            if (loadedClasses.isEmpty()) {
                warning("No source files for transformation.")
                return false
            }

            process(classPool, loadedClasses)

        } catch (e: Exception) {
            e.printStackTrace()
            severe("Could not execute transformation")
            throw GradleException("Could not execute transformation", e)
        }

        return true
    }

    private fun process(classPool: ClassPool, classesList: List<Pair<CtClass, String>>) {
        classesList.forEach { classWithInput ->
            val inputDir = classWithInput.second
            val clazz = classWithInput.first
            val outputDir = getOutputDirForFilePath(inputDir)

            processClass(classPool, clazz, outputDir)
        }
    }

    private fun processClass(classPool: ClassPool, clazz: CtClass, outputDir: String) {
        try {
            if (!transformation.shouldTransform(classPool, clazz)) {
                return
            }
            clazz.defrost()
            transformation.applyTransformations(classPool, clazz)
            clazz.writeFile(outputDir)
        } catch (e: Exception) {
            throw GradleException("An error occurred while trying to process class file ", e)
        }
    }

    private fun setupClassPool(): ClassPool {
        val classPool: ClassPool = AnnotationLoadingClassPool()
        // set up the classpath for the classpool
        globalClassPath.forEach { classpath ->
            classPool.appendClassPath(classpath.path)
        }
        return classPool
    }

    fun preloadClasses(classPool: ClassPool): List<Pair<CtClass, String>> {
        // add the files to process
        val classes = mutableListOf<Pair<CtClass, String>>()
        val loaded = loadClassesForTransformations(classPool)
        classes.addAll(loaded)
        return loaded
    }

    private fun loadClassesForTransformations(classPool: ClassPool): List<Pair<CtClass, String>> {
        val filesForTransformation = transformationsClassPath.asFileTree.filter { file ->
            file.extension == "class"
        }.toList()

        return filesForTransformation.map { file ->
            val clazz = loadClassFile(classPool, file)
            val inputDir = getRootDirOfInputClassFile(file, clazz)

            return@map Pair(clazz, inputDir)
        }
    }

    private fun getRootDirOfInputClassFile(inputClassFile: File, clazz: CtClass): String {
        val fileName = inputClassFile.nameWithoutExtension
        val packageComponents = clazz.packageName
            .replace(".${fileName}", "")
            .split(".")
            .toTypedArray()
        val packageNameAsDir = Paths.get("", *packageComponents).toString()

        return inputClassFile.parent
            .normaliseLineSeparators()
            .replace(packageNameAsDir, "")
    }

    private fun getOutputDirForFilePath(filePath: String): String {
        val pathWithoutBuild = filePath.replace(buildDir, "")
        return Paths.get(rootDestinationDir, pathWithoutBuild).toString()
    }

    private fun loadClassFile(pool: ClassPool, classFile: File): CtClass {
        // read the file first to get the classname
        // much easier than trying to extrapolate from the filename (i.e. with anonymous classes etc.)
        val stream: InputStream = DataInputStream(BufferedInputStream(FileInputStream(classFile)))
        val clazz = pool.makeClass(stream)
        stream.close()
        return clazz
    }

    /**
     * This class loader will load annotations encountered in loaded classes
     * using the pool itself.
     *
     * @see [Javassist issue 18](https://github.com/jboss-javassist/javassist/pull/18)
     */
    private class AnnotationLoadingClassPool : ClassPool(true) {
        override fun getClassLoader(): ClassLoader {
            return Loader(this)
        }
    }
}