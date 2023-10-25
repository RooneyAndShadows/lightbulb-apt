package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.api.IClassTransformer
import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import java.io.*
import java.util.*

internal class TransformationAction(
    private val destinationDir: File?,
    sources: Collection<File>,
    classpath: Collection<File>,
    transformation: IClassTransformer?,
) {
    private val transformation: IClassTransformer?
    private val sources: MutableList<File> = mutableListOf()
    private val classpath: MutableCollection<File> = mutableListOf()

    init {
        this.sources.addAll(sources)
        this.classpath.addAll(classpath)
        this.transformation = transformation
    }


    fun execute(): Boolean {
        // no op if no transformation defined
        if (transformation == null) {
            println("No transformation defined for this task")
            return false
        }
        if (sources.isEmpty()) {
            println("No source files.")
            return false
        }
        if (destinationDir == null) {
            println("No destination directory set")
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
        if (!classpath.isEmpty()) {
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
                clazz.writeFile(destinationDir.toString())
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