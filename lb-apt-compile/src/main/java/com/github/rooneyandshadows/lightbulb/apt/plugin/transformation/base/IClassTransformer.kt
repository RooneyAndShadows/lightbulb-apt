package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base

import javassist.ClassPool
import javassist.CtClass

abstract class IClassTransformer {
    fun transform(classPool: ClassPool, ctClass: CtClass): ByteArray {
        ctClass.defrost()
        if (shouldTransform(classPool, ctClass)) {
            applyTransformations(classPool, ctClass)
        }
        ctClass.freeze()
        return ctClass.toBytecode()
    }

    protected abstract fun applyTransformations(classPool: ClassPool, ctClass: CtClass)

    protected abstract fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean
}