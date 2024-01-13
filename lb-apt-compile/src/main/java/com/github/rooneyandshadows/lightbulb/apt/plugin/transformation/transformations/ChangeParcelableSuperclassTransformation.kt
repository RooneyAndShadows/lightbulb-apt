package com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbParcelable
import com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames
import com.github.rooneyandshadows.lightbulb.apt.plugin.transformation.transformations.base.ClassTransformation
import javassist.*

internal class ChangeParcelableSuperclassTransformation(
    packageNames: PackageNames
) : ClassTransformation(packageNames) {

    @Override
    override fun applyTransformations(classPool: ClassPool, ctClass: CtClass): Result {
        val targetCtClass = getTargetClass(classPool, ctClass)

        ctClass.superclass = targetCtClass

        assureConstructorWithParcel(classPool, ctClass)

        val creatorClass = createCreatorClass(classPool, ctClass)
        val field = CtField(creatorClass, "CREATOR", ctClass)

        field.modifiers = field.modifiers or Modifier.STATIC or Modifier.PUBLIC or Modifier.FINAL

        ctClass.addField(field, CtField.Initializer.byExpr("new ${creatorClass.name}();"))

        removeFieldsWithSetterOrGetter(ctClass)

        val transformationResult = Result(ctClass, true)
        transformationResult.addNewClass(creatorClass)

        return transformationResult
    }

    private fun assureConstructorWithParcel(classPool: ClassPool, ctClass: CtClass) {
        val parcelClass = classPool.getCtClass("android.os.Parcel")
        try {
            val constructor = ctClass.getDeclaredConstructor(arrayOf(parcelClass))
            if (!constructor.callsSuper()) {
                constructor.insertBeforeBody("super($1);")
            }
        } catch (e: NotFoundException) {
            val body = "super($1);"
            val constructor = CtNewConstructor.make(arrayOf(parcelClass), emptyArray(), body, ctClass)
            ctClass.addConstructor(constructor)
            //createConstructor
        }
    }

    private fun createCreatorClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val arrayClass = classPool.getCtClass("${ctClass.name}[]")
        val intClass = classPool.getCtClass("int")
        val parcelClass = classPool.getCtClass("android.os.Parcel")
        val parcelableCreatorClass = classPool.getCtClass("android.os.Parcelable\$Creator")
        val creatorClass = ctClass.makeNestedClass("Creator", true)
        val createFromParcelMethod = CtMethod(ctClass, "createFromParcel", arrayOf(parcelClass), creatorClass)
        val newArrayMethod = CtMethod(arrayClass, "newArray", arrayOf(intClass), creatorClass)
        creatorClass.addInterface(parcelableCreatorClass)

        createFromParcelMethod.setBody("return new ${ctClass.name}($1);")
        newArrayMethod.setBody("return new ${ctClass.name}[$1];")

        creatorClass.addMethod(createFromParcelMethod)
        creatorClass.addMethod(newArrayMethod)

        return creatorClass
    }

    @Override
    override fun shouldTransform(classPool: ClassPool, ctClass: CtClass): Boolean {
        return ctClass.hasAnnotation(LightbulbParcelable::class.java)
    }

    private fun getTargetClass(classPool: ClassPool, ctClass: CtClass): CtClass {
        val simpleName = ctClass.simpleName
        val className = packageNames.parcelablePackage
            .plus(".")
            .plus(GeneratedClassNames.DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX)
            .plus(simpleName)

        return classPool.getCtClass(className)
    }
}