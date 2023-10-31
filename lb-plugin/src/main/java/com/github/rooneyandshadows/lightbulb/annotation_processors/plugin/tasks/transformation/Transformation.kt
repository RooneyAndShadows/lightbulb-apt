package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import java.io.*
import java.nio.file.Paths

@Suppress("MemberVisibilityCanBePrivate")
class Transformation(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val classPathDirectories: List<FileCollection>,
    private val transformation: IClassTransformer
) {

    fun execute(): Boolean {
        println("Executing transformations: ${transformation.javaClass.name}")
        try {
            val loadedClasses = preloadClasses()

            if (loadedClasses.isEmpty()) {
                println("No source files.")
                return false
            }

            process(loadedClasses)
        } catch (e: Exception) {
            throw GradleException("Could not execute transformation", e)
        }

        return true
    }

    private fun process(classes: Map<String, List<CtClass>>) {
        classes.forEach { (outputDirectory, classesList) ->
            classesList.forEach { clazz ->
                processClass(clazz, outputDirectory)
            }
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

    fun preloadClasses(): Map<String, List<CtClass>> {
        val classPool: ClassPool = AnnotationLoadingClassPool()
        // set up the classpath for the classpool
        classPathDirectories.forEach { classpath ->
            classPool.appendClassPath(classpath.asPath)
        }
        // add the files to process
        val allLoaded = mutableMapOf<String, List<CtClass>>()
        classPathDirectories.forEach { fileCollection ->
            val outputDir = getOutputDirForClassPath(fileCollection)
            val loaded = loadClassesForClassPath(classPool, fileCollection)
            allLoaded.computeIfAbsent(outputDir) { mutableListOf() }.apply {
                (this as MutableList<CtClass>).addAll(loaded)
            }
        }
        return allLoaded
    }

    private fun loadClassesForClassPath(classPool: ClassPool, classPath: FileCollection): List<CtClass> {
        return classPath.asFileTree.filter { file ->
            return@filter file.extension == "class"
        }.map { loadClassFile(classPool, it) }
    }

    private fun getOutputDirForClassPath(classPath: FileCollection): String {
        val inputPath = classPath.asPath
        val pathWithoutBuild = inputPath.replace(buildDir, "")
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