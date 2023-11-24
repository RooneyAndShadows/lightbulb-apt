package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import org.gradle.configurationcache.extensions.capitalized


internal class ChangeFragmentSuperclassTransformation : IClassTransformer() {
    private val generatedTargetClassLocation = PackageNames.getFragmentsPackage()

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))

        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.declaredFields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            val capitalizedName = it.name.capitalized()
            val setterName = "set${capitalizedName}"
            val getterName = "get${capitalizedName}"
            ctClass.declaredMethods.forEach { method ->
                val methodName = method.name
                if (methodName == setterName || methodName == getterName) {
                    if (isMethodPrivate(method)) {
                        val modifiers = Modifier.setProtected(method.modifiers)
                        method.modifiers = modifiers
                    }
                }
            }
            if (isFragmentParameter || isFragmentPersistedVar) {
                ctClass.removeField(it)
            }
        }

        ctClass.superclass = targetCtClass
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbFragment::class.java)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = generatedTargetClassLocation
            .plus(".")
            .plus(GENERATED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }

    private fun isMethodPrivate(ctMethod: CtMethod): Boolean {
        return Modifier.isPrivate(ctMethod.modifiers)
    }
}