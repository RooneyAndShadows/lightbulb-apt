package com.github.rooneyandshadows.lightbulb.annotation_processors

import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation.IClassTransformer
import javassist.CtClass
import javassist.CtMethod


internal class MyTransformation : IClassTransformer {
    override fun applyTransformations(ctClass: CtClass) {
        println("Transforming class:".plus(ctClass.name))
        //ctClass.name = "dedovia." + ctClass.name
        val m: CtMethod = ctClass.getDeclaredMethod("onCreate")
        m.insertBefore("{ System.out.println(\"asfasfasf\"); System.out.println(\"asfasasfasf\"); }")
    }

    override fun shouldTransform(ctClass: CtClass): Boolean {
        return ctClass.methods.toList().any { ctMethod -> ctMethod.name == "onCreate" }
        return true
    }
}