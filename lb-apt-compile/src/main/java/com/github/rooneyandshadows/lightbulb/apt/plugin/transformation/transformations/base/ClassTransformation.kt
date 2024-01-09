package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames
import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil
import javassist.*

abstract class ClassTransformation(protected val packageNames: PackageNames) {

    fun transform(classPool: ClassPool, ctClass: CtClass): Result {
        if (!shouldTransform(classPool, ctClass)) {
            return Result(ctClass, false)
        }

        LoggingUtil.info("Executing transformation: ${javaClass.name}")

        ctClass.defrost()
        try {
            LoggingUtil.info("Transforming class:".plus(ctClass.name))

            return applyTransformations(classPool, ctClass)
        } finally {
            ctClass.freeze()
        }
    }

    protected abstract fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Result

    protected abstract fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean

    protected fun isPackageProtected(member: CtMember): Boolean {
        return Modifier.isPackage(member.modifiers)
    }

    protected fun isPrivate(member: CtMember): Boolean {
        return Modifier.isPrivate(member.modifiers)
    }

    protected fun isProtected(member: CtMember): Boolean {
        return Modifier.isProtected(member.modifiers)
    }

    protected fun isPublic(member: CtMember): Boolean {
        return Modifier.isProtected(member.modifiers)
    }

    protected fun setPackageProtected(member: CtMember) {
        val modifiers = Modifier.setPackage(member.modifiers)
        member.modifiers = modifiers
    }

    protected fun setPrivate(member: CtMember) {
        val modifiers = Modifier.setPrivate(member.modifiers)
        member.modifiers = modifiers
    }

    protected fun setProtected(member: CtMember) {
        val modifiers = Modifier.setProtected(member.modifiers)
        member.modifiers = modifiers
    }

    protected fun setPublic(member: CtMember) {
        val modifiers = Modifier.setPublic(member.modifiers)
        member.modifiers = modifiers
    }

    class Result(val targetClass: CtClass, val modified: Boolean) {
        val newClasses: Set<CtClass> = mutableSetOf()

        public fun addNewClass(clazz: CtClass) {
            (newClasses as MutableSet).add(clazz)
        }
    }
}