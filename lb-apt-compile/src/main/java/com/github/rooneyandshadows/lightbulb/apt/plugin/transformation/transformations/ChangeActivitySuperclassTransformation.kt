package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity
import com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
import javassist.ClassPool
import javassist.CtClass

internal class ChangeActivitySuperclassTransformation(
    packageNames: com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames,
) : ClassTransformation(packageNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Result {
        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.superclass = targetCtClass
        return Result(ctClass, true)
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbActivity::class.java)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = packageNames.activitiesPackage
            .plus(".")
            .plus(DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}