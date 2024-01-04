package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import org.gradle.api.file.FileCollection
import java.util.jar.JarOutputStream

class TransformationJobRegistry(
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: (() -> FileCollection),
) {
    private val transformations: MutableList<IClassTransformer> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        transformations.add(transformer)
    }

    fun execute(
        jarDestination: JarOutputStream,
        onClassSaved: ((classDir: String, className: String, byteCode: ByteArray) -> Unit)? = null
    ) {
        val executor = TransformationExecutor(globalClassPath, transformationsClassPath, transformations)
        executor.execute(jarDestination, onClassSaved)
    }
}