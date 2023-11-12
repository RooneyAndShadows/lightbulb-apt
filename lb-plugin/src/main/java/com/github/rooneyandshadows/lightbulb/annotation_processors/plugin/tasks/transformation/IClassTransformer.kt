package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.transformation

import javassist.ClassPool
import javassist.CtClass

interface IClassTransformer {
    fun applyTransformations(classPool: ClassPool, ctClass: CtClass)

    fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean
}