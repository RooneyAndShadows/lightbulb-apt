package com.github.rooneyandshadows.lightbulb.annotation_processors

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.api.IClassTransformer
import javassist.CtClass

internal class MyTransformation : IClassTransformer {
    override fun applyTransformations(clazz: CtClass?) {
        clazz!!.name = "test.package." + clazz.name
    }

    override fun shouldTransform(ctClass: CtClass?): Boolean {
        return true
    }
}