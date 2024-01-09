package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.job

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
import org.gradle.api.file.FileCollection

class TransformationJobRegistry(
    private val globalClassPath: FileCollection,
    private val transformationsClassPath: (() -> FileCollection)
) {
    private val transformations: MutableList<ClassTransformation> = mutableListOf()

    fun register(transformer: ClassTransformation) {
        transformations.add(transformer)
    }

    fun execute(onClassSaved: ((classDir: String, className: String, modified: Boolean, byteCode: ByteArray) -> Unit)) {
        val executor = TransformationExecutor(globalClassPath, transformationsClassPath, transformations)
        executor.execute(onClassSaved)
    }
}