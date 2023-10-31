package com.github.rooneyandshadows.lightbulb.annotation_processors

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation.IClassTransformer
import javassist.CtClass

internal class MyTransformation : IClassTransformer {
    override fun applyTransformations(ctClass: CtClass) {
       // clazz!!.name = "test.package." + clazz.name
        println("Transforming class:".plus(ctClass.name))
    }

    override fun shouldTransform(ctClass: CtClass): Boolean {
        return true
    }
}