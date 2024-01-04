package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.ClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbApplication
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass

internal class ChangeApplicationSuperclassTransformation(
    packageNames: PackageNames,
    classNames: ClassNames
) : ClassTransformer(packageNames, classNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Set<CtClass> {
        info("Transforming class:".plus(ctClass.name))

        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.superclass = targetCtClass

        return emptySet();
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbApplication::class.java)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = packageNames.applicationPackage
            .plus(".")
            .plus(DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}