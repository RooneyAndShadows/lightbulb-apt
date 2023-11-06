package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import java.io.*
import java.nio.file.Paths

@Suppress("MemberVisibilityCanBePrivate")
class Transformation(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val classPath: FileCollection,
    private val transformation: IClassTransformer
) {

    fun execute(): Boolean {

        println("Executing transformations: ${transformation.javaClass.name}")
        try {
            val loadedClasses = preloadClasses(classPath)

            if (loadedClasses.isEmpty()) {
                println("No source files.")
                return false
            }

            process(loadedClasses)

        } catch (e: Exception) {
            e.printStackTrace()
            throw GradleException("Could not execute transformation", e)
        }

        return true
    }

    private fun process(classesList: List<Pair<CtClass, String>>) {
        classesList.forEach { classWithInput ->
            val inputDir = classWithInput.second
            val clazz = classWithInput.first
            val outputDir = getOutputDirForFilePath(inputDir)

            processClass(clazz, outputDir)
        }
    }

    private fun processClass(clazz: CtClass, outputDir: String) {
        try {
            if (!transformation.shouldTransform(clazz)) {
                return
            }
            clazz.defrost()
            transformation.applyTransformations(clazz)
            clazz.writeFile(outputDir)
        } catch (e: Exception) {
            throw GradleException("An error occurred while trying to process class file ", e)
        }
    }

    fun preloadClasses(targetClassPath: FileCollection): List<Pair<CtClass, String>> {
        val classPool: ClassPool = AnnotationLoadingClassPool()
        // set up the classpath for the classpool
        classPath.forEach { classpath ->
            classPool.appendClassPath(classpath.path)
        }

        // add the files to process
        val classes = mutableListOf<Pair<CtClass, String>>()
        val loaded = loadClassesForClassPath(classPool, targetClassPath)
        classes.addAll(loaded)
        return loaded
    }

    private fun loadClassesForClassPath(classPool: ClassPool, classPath: FileCollection): List<Pair<CtClass, String>> {
        return classPath.asFileTree.filter { file -> return@filter file.extension == "class" }
            .map { file ->
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