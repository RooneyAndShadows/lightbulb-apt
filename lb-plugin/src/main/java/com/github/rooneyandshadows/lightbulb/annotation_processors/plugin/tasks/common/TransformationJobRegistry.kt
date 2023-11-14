package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.common

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.VariantOutput
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.base.IClassTransformer

class TransformationJobRegistry(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val variantOutput: VariantOutput
) {
    private val transformations: MutableList<TransformationJob> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = TransformationJob(buildDir, rootDestinationDir, variantOutput, transformer)
        transformations.add(transformation)
    }

    fun execute() {
        try {
            transformations.forEach { it.execute() }
        } finally {
            transformations.clear()
        }
    }
}