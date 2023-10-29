package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.SourceSetTransformation
import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import java.io.*
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class Transformation(
    private val sourceSets: List<SourceSetTransformation>,
    private val transformation: IClassTransformer? = null,
) {
    private val sources: List<File>
        get() = sourceSets.flatMap { return@flatMap it.classFiles }

    fun execute(): Boolean {
        println("Executing transformations: ${transformation?.javaClass?.name}")
        if (transformation == null) {
            println("No transformation defined for this task")
            return false
        }
        if (sources.isEmpty()) {
            println("No source files.")
            return false
        }
        sourceSets.forEach { sourceSet ->
            try {
                val loadedClasses = preloadClasses(sourceSet)
                process(loadedClasses, sourceSet.destinationDir)
            } catch (e: Exception) {
                throw GradleException("Could not execute transformation", e)
            }
        }
        return true
    }


    private fun preloadClasses(sourceSet: SourceSetTransformation): List<CtClass> {
        val loadedClasses: MutableList<CtClass> = LinkedList()
        val pool: ClassPool = AnnotationLoadingClassPool()

        // set up the classpath for the classpool
        pool.appendClassPath(sourceSet.classesDir)

        // add the files to process
        sourceSet.classFiles.forEach { file ->
            if (!file.isDirectory) {
                loadedClasses.add(loadClassFile(pool, file))
            }
        }

        return loadedClasses
    }

    private fun process(classes: Collection<CtClass>, outputDirectory: String) {
        for (clazz in classes) {
            processClass(clazz, outputDirectory)
        }
    }

    private fun processClass(clazz: CtClass, outputDirectory: String) {
        try {
            if (transformation!!.shouldTransform(clazz)) {
                clazz.defrost()
                transformation.applyTransformations(clazz)
                clazz.writeFile(outputDirectory)
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