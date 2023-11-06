package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.common.VariantOutput

class TransformationRegistry(
    private val buildDir: String,
    private val rootDestinationDir: String,
    private val variantOutput: VariantOutput
) {
    private val transformations: MutableList<Transformation> = mutableListOf()

    fun register(transformer: IClassTransformer) {
        val transformation = Transformation(buildDir, rootDestinationDir, variantOutput, transformer)
        transformations.add(transformation)
    }

    fun execute() {
        transformations.forEach { it.execute() }
    }
}