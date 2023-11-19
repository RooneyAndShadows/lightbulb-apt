package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.base

import javassist.ClassPool
import javassist.CtClass

abstract class IClassTransformer {
    fun transform(classPool: ClassPool, ctClass: CtClass): ByteArray {
        ctClass.defrost()
        if (shouldTransform(classPool, ctClass)) {
            applyTransformations(classPool, ctClass)
        }
        return ctClass.toBytecode()
        //ctClass.writeFile(outputDir)
    }

    protected abstract fun applyTransformations(classPool: ClassPool, ctClass: CtClass)

    protected abstract fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean
}