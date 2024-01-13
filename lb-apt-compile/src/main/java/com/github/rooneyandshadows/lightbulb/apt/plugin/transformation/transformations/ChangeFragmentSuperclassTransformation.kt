package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations

import com.github.rooneyandshadows.lightbulb.apt.annotations.BindView
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
import javassist.*
import org.gradle.configurationcache.extensions.capitalized

internal class ChangeFragmentSuperclassTransformation(
    packageNames: PackageNames,
) : ClassTransformation(packageNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Result {
        val targetCtClass = getTargetClass(classPool, ctClass)

        removeFieldsWithSetterOrGetter(ctClass)filter@{ field ->
            val isFragmentParameter = field.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = field.hasAnnotation(FragmentStatePersisted::class.java)
            val isViewBinding = field.hasAnnotation(BindView::class.java)

            return@filter isFragmentParameter || isFragmentPersistedVar || isViewBinding
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