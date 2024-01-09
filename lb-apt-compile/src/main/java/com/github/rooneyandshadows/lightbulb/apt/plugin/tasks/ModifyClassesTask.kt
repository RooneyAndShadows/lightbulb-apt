package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import javassist.ClassPool
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

abstract class ModifyClassesTask: DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {

        val pool = ClassPool(ClassPool.getDefault())

        val jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(
            output.get().asFile
        )))
        allJars.get().forEach { file ->
            println("handling " + file.asFile.getAbsolutePath())
            val jarFile = JarFile(file.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                println("Adding from jar ${jarEntry.name}")
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
            jarFile.close()
        }
        allDirectories.get().forEach { directory ->
            println("handling " + directory.asFile.getAbsolutePath())
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith("SomeSource.class")) {
                        println("Found $file.name")
                        val interfaceClass = pool.makeInterface("com.android.api.tests.SomeInterface")
                        println("Adding $interfaceClass")
                        jarOutput.putNextEntry(JarEntry("com/android/api/tests/SomeInterface.class"))
                        jarOutput.write(interfaceClass.toBytecode())
                        jarOutput.closeEntry()
                        val ctClass = file.inputStream().use {
                            pool.makeClass(it)
                        }
                        ctClass.addInterface(interfaceClass)

                        val m = ctClass.getDeclaredMethod("toString")
                        if (m != null) {
                            m.insertBefore("{ System.out.println(\"Some Extensive Tracing\"); }")

                            val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                            jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                            jarOutput.write(ctClass.toBytecode())
                            jarOutput.closeEntry()
                        } else {
                            val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                            println("Adding from directory ${relativePath.replace(File.separatorChar, '/')}")
                            jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }
                }
            }
        }
        jarOutput.close()
    }
}