package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.android.tools.r8.internal.cc
import com.github.rooneyandshadows.lightbulb.apt.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ClassFile
import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.Annotation
import org.gradle.configurationcache.extensions.capitalized


internal class ChangeSuperclassTransformation : IClassTransformer() {

    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))
        val simpleName = ctClass.simpleName
        val className = PackageNames.getFragmentsPackage().plus(".").plus("Lightbulb_").plus(simpleName)
        val targetCtClass = classPool.getCtClass(className)

        ctClass.declaredFields.forEach {
            val isFragmentParameter = it.hasAnnotation(FragmentParameter::class.java)
            val isFragmentPersistedVar = it.hasAnnotation(FragmentStatePersisted::class.java)
            val capitalizedName = it.name.capitalized()
            val setterName = "set${capitalizedName}"
            val getterName = "get${capitalizedName}"
            ctClass.declaredMethods.forEach { method ->
                val methodName = method.name
                if (methodName == setterName || methodName == getterName) {
                    val ccFile: ClassFile = ctClass.getClassFile()
                    val cp: ConstPool = ccFile.constPool
                    val attr = AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag)
                    val annotation: Annotation = Annotation(Override::class.java.name, cp)
                    attr.addAnnotation(annotation)
                    method.methodInfo.addAttribute(attr)
                }

            }
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