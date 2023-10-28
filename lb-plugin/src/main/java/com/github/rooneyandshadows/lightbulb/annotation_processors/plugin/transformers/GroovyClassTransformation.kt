package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformers

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.IClassTransformer
import groovy.lang.Closure
import javassist.CtClass

class GroovyClassTransformation(
    private val transform: Closure<Unit>,
    private val filter: Closure<Boolean>? = null
) : IClassTransformer {

    override fun applyTransformations(ctClass: CtClass?) {
        transform.call(ctClass)
    }

    override fun shouldTransform(ctClass: CtClass?): Boolean {
        return filter?.call(ctClass) ?: true
    }
}