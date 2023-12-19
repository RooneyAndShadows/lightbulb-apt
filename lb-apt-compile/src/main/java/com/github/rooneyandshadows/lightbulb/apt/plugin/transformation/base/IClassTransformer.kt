package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil
import javassist.ClassPool
import javassist.CtClass

abstract class IClassTransformer {
    fun transform(classPool: ClassPool, ctClass: CtClass): CtClass {
        if (shouldTransform(classPool, ctClass)) {
            LoggingUtil.info("Executing transformation: ${javaClass.name}")
            ctClass.defrost()
            applyTransformations(classPool, ctClass)
            ctClass.freeze()
        }
        return ctClass
    }

    protected abstract fun applyTransformations(classPool: ClassPool, ctClass: CtClass)

    protected abstract fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean
}