package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation

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
    private val transformation: IClassTransformer? = null,
) {

    fun execute(): Boolean {
        println("Executing transformations: ${transformation?.javaClass?.name}")

        if (transformation == null) {
            println("No transformation defined for this task")
            return false
        }

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


    private fun preloadClasses(): Map<String, List<CtClass>> {
        val loadedClasses: MutableMap<String, MutableList<CtClass>> = mutableMapOf()
        val pool: ClassPool = AnnotationLoadingClassPool()

        // set up the classpath for the classpool
        classPathDirectories.forEach { classpath ->
            pool.appendClassPath(classpath.asPath)
        }

        classPathDirectories.forEach { fileCollection ->
            val inputPath = fileCollection.asPath
            val pathWithoutBuild = inputPath.replace(buildDir, "")
            val outputDir = Paths.get(rootDestinationDir, pathWithoutBuild).toString()
            val classes = loadedClasses.getOrPut(outputDir) { mutableListOf() }

            val classFiles = fileCollection.asFileTree.filter { file -> return@filter file.extension == "class" }
                .toList()
            classFiles.forEach { file ->
                val ctClass = loadClassFile(pool, file)
                classes.add(ctClass)
            }
        }
        // add the files to process


        return loadedClasses
    }

    private fun process(classes: Map<String, List<CtClass>>) {
        classes.forEach { (classDirectory, classesList) ->
            classesList.forEach { clazz ->
                processClass(clazz, classDirectory)
            }

        }
    }

    private fun processClass(clazz: CtClass, outputDir: String) {
        try {
            if (transformation!!.shouldTransform(clazz)) {
                clazz.defrost()
                transformation.applyTransformations(clazz)
                clazz.writeFile(outputDir)
            }
            // println("OUTPUT:" + outputDirectory)
        } catch (e: Exception) {
            throw GradleException("An error occurred while trying to process class file ", e)
        }
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