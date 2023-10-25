package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformers

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.api.IClassTransformer
import groovy.lang.Closure
import javassist.CtClass

class GroovyClassTransformation : IClassTransformer {
    private var transform: Closure<Unit>
    private var filter: Closure<Boolean>?

    constructor(transform: Closure<Unit>) {
        this.transform = transform
        filter = null
    }

    constructor(transform: Closure<Unit>, filter: Closure<Boolean>?) {
        this.transform = transform
        this.filter = filter
    }

    override fun applyTransformations(ctClass: CtClass?) {
        transform.call(ctClass)
    }

    override fun shouldTransform(ctClass: CtClass?): Boolean {
        return filter == null || filter!!.call(ctClass)
    }

    fun transform(transform: Closure<Unit>) {
        this.transform = transform
    }

    fun where(filter: Closure<Boolean>?) {
        this.filter = filter
    }
}