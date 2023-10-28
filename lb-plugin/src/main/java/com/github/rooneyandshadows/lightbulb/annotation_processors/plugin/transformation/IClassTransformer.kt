package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation

import javassist.CtClass

interface IClassTransformer {
    fun applyTransformations(ctClass: CtClass?)

    fun shouldTransform(ctClass: CtClass?): Boolean
}