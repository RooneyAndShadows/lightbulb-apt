package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.job

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil.Companion.severe
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil.Companion.warning
import javassist.ClassPool
import javassist.CtClass
import javassist.Loader
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import java.io.*

@Suppress("MemberVisibilityCanBePrivate")
class TransformationExecutor(
    private val globalClassPath: FileCollection,
    private val transformationsClassPathProvider: (() -> FileCollection),
    private val transformations: List<ClassTransformation>,
) {
    private val transformationsClassPath: FileCollection
        get() = transformationsClassPathProvider.invoke()

    fun execute(action: ((packageName: String, className: String, modified: Boolean, byteCode: ByteArray) -> Unit)): Boolean {
        var result = false

        val classPool = setupClassPool()
        val loadedClasses = preloadClasses(classPool)

        if (loadedClasses.isEmpty()) {
            warning("No source files for transformation.")
        }

        loadedClasses.forEach { ctClass ->
            var isModified = false
            val classesToAdd:MutableSet<CtClass> = mutableSetOf()

            transformations.forEach { transformation ->
                try {
                    transformation.transform(classPool, ctClass).apply {
                        isModified = isModified || this.modified
                        classesToAdd.addAll(newClasses)
                    }

                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    severe("Could not execute transformation")
                    throw GradleException("Could not execute transformation", e)
                }
            }

            classesToAdd.forEach { cls ->
                onTransformed(cls, true, action)
            }

            onTransformed(ctClass, isModified, action)
        }

        return result
    }

    private fun onTransformed(
        targetClass: CtClass,
        isNewOrModified: Boolean,
        action: ((packageName: String, className: String, modified: Boolean, byteCode: ByteArray) -> Unit)
    ) {
        try {
            val classSimpleName = targetClass.simpleName
            val packageToDirectory = targetClass.packageName.replace(".", "/")
            val byteCode = targetClass.toBytecode()

            action.invoke(packageToDirectory, classSimpleName, isNewOrModified, byteCode)

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