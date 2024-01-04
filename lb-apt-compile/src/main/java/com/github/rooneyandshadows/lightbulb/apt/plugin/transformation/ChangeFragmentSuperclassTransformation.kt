package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.ClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.BindView
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.*
import org.gradle.configurationcache.extensions.capitalized


internal class ChangeFragmentSuperclassTransformation(
    packageNames: PackageNames,
    classNames: ClassNames
) : ClassTransformer(packageNames, classNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Set<CtClass> {
        info("Transforming class:".plus(ctClass.name))

        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.declaredFields.forEach { field ->
            val isFragmentParameter = field.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = field.hasAnnotation(FragmentStatePersisted::class.java)
            val isViewBinding = field.hasAnnotation(BindView::class.java)

            if (!isFragmentParameter && !isFragmentPersistedVar && !isViewBinding) {
                return@forEach
            }

            val capitalizedName = field.name.capitalized()
            val setterName = "set${capitalizedName}"
            val getterName = "get${capitalizedName}"
            var removeField = false

            ctClass.declaredMethods.forEach { method ->
                val currentMethodName = method.name
                if (currentMethodName == setterName || currentMethodName == getterName) {
                    removeField = true
                    if (isMethodPrivate(method)) {
                        val modifiers = Modifier.setProtected(method.modifiers)
                        method.modifiers = modifiers
                    }
                }
            }

            if (removeField) {
                ctClass.removeField(field)
            }
        }

        ctClass.superclass = targetCtClass

        return emptySet()
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbFragment::class.java)
    }

    private fun isMethodPrivate(ctMethod: CtMethod): Boolean {
        return Modifier.isPrivate(ctMethod.modifiers)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = packageNames.fragmentsPackage
            .plus(".")
            .plus(DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}