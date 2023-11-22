package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.getAnnotationHandler
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbTransformation
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.annotation.ClassMemberValue


internal class ChangeSuperclassTransformation : IClassTransformer() {

    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))

        val transformationInfo = ctClass.getAnnotationHandler(LightbulbTransformation::class.java)!!
        val classMemberValue = (transformationInfo.getMemberValue("target") as ClassMemberValue).value
        val targetCtClass = classPool.getCtClass(classMemberValue)

        targetCtClass.declaredFields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            if (isFragmentParameter || isFragmentPersistedVar) {
                targetCtClass.removeField(it)
            }
        }

        targetCtClass.superclass = ctClass
    }

    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbTransformation::class.java)
    }
}