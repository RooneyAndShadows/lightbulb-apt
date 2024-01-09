package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations

import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
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
) : ClassTransformation(packageNames, classNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Result {
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
                    if (isPrivate(method)) {
                        setProtected(method)
                    }
                }
            }

            if (removeField) {
                ctClass.removeField(field)
            }
        }

        ctClass.superclass = targetCtClass

        return Result(ctClass, true)
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbFragment::class.java)
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