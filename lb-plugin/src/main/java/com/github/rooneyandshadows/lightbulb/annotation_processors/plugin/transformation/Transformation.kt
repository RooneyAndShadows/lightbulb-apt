package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation

import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.*
import java.nio.file.Paths
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class Transformation(
    private val project: Project,
    private val inputDir: String,
    private val outputDir: File,
    private val transformation: IClassTransformer? = null,
) {
    private val classesDir: FileCollection
        get() = project.files(inputDir)
    private val classpath: List<File>
        get() = classesDir.map { return@map project.file(it) }
    private val classFiles: FileCollection
        get() = classesDir.asFileTree.filter { return@filter it.extension == "class" }
    private val sources: List<File>
        get() = classFiles.files.toList()

    fun execute(): Boolean {
        if (transformation == null) {
            println("No transformation defined for this task")
            return false
        }
        if (sources.isEmpty()) {
            println("No source files.")
            return false
        }
        try {
            val loadedClasses = preloadClasses()
            process(loadedClasses)
        } catch (e: Exception) {
            throw GradleException("Could not execute transformation", e)
        }
        return true
    }


    private fun preloadClasses(): List<CtClass> {
        val loadedClasses: MutableList<CtClass> = LinkedList()
        val pool: ClassPool = AnnotationLoadingClassPool()

        // set up the classpath for the classpool
        if (classpath.isNotEmpty()) {
            for (f in classpath) {
                pool.appendClassPath(f.toString())
            }
        }

        // add the files to process
        sources.forEach { file ->
            if (!file.isDirectory) {
                loadedClasses.add(loadClassFile(pool, file))
            }
        }

        return loadedClasses
    }

    private fun process(classes: Collection<CtClass>) {
        for (clazz in classes) {
            processClass(clazz)
        }
    }

    private fun processClass(clazz: CtClass) {
        try {
            if (transformation!!.shouldTransform(clazz)) {
                clazz.defrost()
                transformation.applyTransformations(clazz)
                clazz.writeFile(outputDir.path)
            }
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