package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.severe
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.warning
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

@Suppress("MemberVisibilityCanBePrivate")
class TransformationExecutor(
    private val globalClassPath: FileCollection,
    private val transformationsClassPathProvider: (() -> FileCollection),
    private val transformations: List<IClassTransformer>,
) {
    private val transformationsClassPath: FileCollection
        get() = transformationsClassPathProvider.invoke()

    fun execute(jarDestination: JarOutputStream): Boolean {
        var result = false

        val classPool = setupClassPool()
        val loadedClasses = preloadClasses(classPool)

        if (loadedClasses.isEmpty()) {
            warning("No source files for transformation.")
        }

        loadedClasses.forEach { ctClass ->
            transformations.forEach { transformation ->
                try {
                    transformation.transform(classPool, ctClass)
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    severe("Could not execute transformation")
                    throw GradleException("Could not execute transformation", e)
                }
            }

            sendToDestination(ctClass, jarDestination)
        }

        return result
    }

    private fun sendToDestination(targetClass: CtClass, jarDestination: JarOutputStream) {
        try {
            val className = targetClass.name
            info("Adding to jar $className")
            jarDestination.putNextEntry(JarEntry(className.replace(".", "/").plus(".class")))
            jarDestination.write(targetClass.toBytecode())
            jarDestination.closeEntry()
        } catch (e: Exception) {
            throw GradleException("An error occurred while trying to process class file ", e)
        }
    }

    private fun setupClassPool(): ClassPool {
        val classPool = AnnotationLoadingClassPool()
        // set up the classpath for the classPool
        globalClassPath.forEach { classpath ->
            classPool.appendClassPath(classpath.path)
        }
        return classPool
    }

    fun preloadClasses(classPool: ClassPool): List<CtClass> {
        // add the files to process
        val filesForTransformation = transformationsClassPath.asFileTree.filter { file ->
            file.extension == "class"
        }.toList()
        return filesForTransformation.map { file -> loadClassFile(classPool, file) }
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