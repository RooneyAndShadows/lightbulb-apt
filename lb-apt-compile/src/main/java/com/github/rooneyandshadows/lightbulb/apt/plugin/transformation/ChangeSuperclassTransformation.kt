package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass

internal class ChangeSuperclassTransformation : IClassTransformer() {

    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))
        val simpleName = ctClass.simpleName
        val className = PackageNames.getFragmentsPackage().plus(".").plus("Lightbulb_").plus(simpleName)
        val targetCtClass = classPool.getCtClass(className)
        ctClass.declaredFields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            if (isFragmentParameter || isFragmentPersistedVar) {
                ctClass.removeField(it)
            }
        }

        ctClass.superclass = targetCtClass
    }

    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbFragment::class.java)
    }
}