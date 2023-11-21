package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbTransformation
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_FRAGMENTS_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass

internal class ChangeSuperclassTransformation : IClassTransformer() {

    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))
        ctClass.annotations.forEach {
            println(it.javaClass)
        }
        val transformationInfo = ctClass.getAnnotation(LightbulbTransformation::class.java) as LightbulbTransformation
        val targetClass = classPool.getCtClass(transformationInfo.target.qualifiedName)

        targetClass.fields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            if (isFragmentParameter || isFragmentPersistedVar) {
                targetClass.removeField(it)
            }
        }

        targetClass.superclass = ctClass
    }

    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbTransformation::class.java)
    }
}