package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil
import javassist.ClassPool
import javassist.CtClass

abstract class IClassTransformer {
    fun transform(classPool: ClassPool, ctClass: CtClass): Set<CtClass> {
        if (!shouldTransform(classPool, ctClass)) {
            return setOf();
        }
        LoggingUtil.info("Executing transformation: ${javaClass.name}")

        ctClass.defrost()
        try {
            return applyTransformations(classPool, ctClass)
        } finally {
            ctClass.freeze()
        }
    }

    protected abstract fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Set<CtClass>

    protected abstract fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean
}