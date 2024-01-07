package com.github.rooneyandshadows.lightbulb.apt.plugin.tasks.transform.common

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.ClassTransformer
import org.gradle.api.file.FileCollection

class TransformationJobRegistry(
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: (() -> FileCollection)
) {
    private val transformations: MutableList<ClassTransformer> = mutableListOf()

    fun register(transformer: ClassTransformer) {
        transformations.add(transformer)
    }

    fun execute(onClassSaved: ((classDir: String, className: String, byteCode: ByteArray) -> Unit)) {
        val executor = TransformationExecutor(globalClassPath, transformationsClassPath, transformations)
        executor.execute(onClassSaved)
    }
}