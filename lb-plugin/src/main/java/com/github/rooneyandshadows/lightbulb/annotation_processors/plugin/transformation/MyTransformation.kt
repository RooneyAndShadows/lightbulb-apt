package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger.LoggingUtil.Companion.info
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.base.IClassTransformer
import javassist.ClassPool
import javassist.CtClass

internal class MyTransformation : IClassTransformer {
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass) {
        info("Transforming class:".plus(ctClass.name))
        val superClass = classPool.getCtClass("com.example.myapplication.BaseActivity")
        ctClass.superclass = superClass
        //val m: CtMethod = ctClass.getDeclaredMethod("onCreate")
        //m.insertBefore("{ System.out.println(\"asfasfasf\"); System.out.println(\"asfasasfasf\"); }")
    }

    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        val hasAnnotation =
            ctClass.hasAnnotation("com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration")
        val hasOnCreate = ctClass.methods.toList().any { ctMethod -> ctMethod.name == "onCreate" }
        return hasAnnotation
    }
}