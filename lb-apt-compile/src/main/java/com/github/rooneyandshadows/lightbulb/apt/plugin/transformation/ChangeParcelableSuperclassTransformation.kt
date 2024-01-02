package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation

import com.github.rooneyandshadows.lightbulb.apt.plugin.utils.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.base.IClassTransformer
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.BindView
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.FragmentParameter
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.FragmentStatePersisted
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbParcelable
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames
import javassist.*
import org.gradle.configurationcache.extensions.capitalized


internal class ChangeParcelableSuperclassTransformation : IClassTransformer() {
    private val generatedTargetClassLocation = PackageNames.getFragmentsPackage()

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Set<CtClass> {
        info("Transforming class:".plus(ctClass.name))

        val targetCtClass = getTargetClass(classPool, ctClass)

        //  val creatorField = CtField(,)

        // targetCtClass.addField()

        ctClass.superclass = targetCtClass

        return emptySet()
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbParcelable::class.java)
    }


    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = generatedTargetClassLocation
            .plus(".")
            .plus(DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}