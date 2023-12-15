package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbActivity
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass

internal class ChangeActivitySuperclassTransformation : IClassTransformer() {
    private val generatedTargetClassLocation = PackageNames.getActivitiesPackage()

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))

        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.superclass = targetCtClass
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbActivity::class.java)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = generatedTargetClassLocation
            .plus(".")
            .plus(DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}