package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_FRAGMENTS_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass

internal class ChangeSuperclassTransformation : IClassTransformer() {

    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))
        val superClassPackage = PackageNames.getFragmentsPackage();
        val superClassSimpleName = GENERATED_FRAGMENTS_CLASS_NAME_PREFIX.plus(ctClass.simpleName)
        val superClassName = superClassPackage.plus(".").plus(superClassSimpleName)
        val superClass = classPool.getCtClass(superClassName)

        ctClass.superclass = superClass

        ctClass.fields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            if (isFragmentParameter || isFragmentPersistedVar) {
                ctClass.removeField(it)
            }
        }
    }

    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbFragment::class.java)
    }
}