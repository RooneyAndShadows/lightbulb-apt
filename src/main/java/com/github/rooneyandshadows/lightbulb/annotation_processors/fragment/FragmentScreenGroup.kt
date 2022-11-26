package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment

import com.squareup.kotlinpoet.*
import java.util.function.Consumer

class FragmentScreenGroup(screenGroupName: String?) {
    val screenGroupName: String
    val screens = ArrayList<FragmentInfo>()

    init {
        var screenGroupName = screenGroupName
        if (screenGroupName == null || screenGroupName == "") screenGroupName = "Common"
        this.screenGroupName = screenGroupName
    }

    fun addScreen(fragmentInfo: FragmentInfo) {
        screens.add(fragmentInfo)
    }

    fun build(): TypeSpec {
        val baseRouterClass = ClassName(
            "com.github.rooneyandshadows.lightbulb.application.activity.routing",
            "BaseActivityRouter"
        )
        val fragmentScreenClass = baseRouterClass.nestedClass("FragmentScreen")
        val groupClass = TypeSpec.classBuilder(screenGroupName)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
        screens.forEach(Consumer { fragmentInfo: FragmentInfo ->
            val screenConstructor: FunSpec.Builder = FunSpec
                .constructorBuilder()
                .addModifiers(KModifier.PUBLIC)
            val screenClass = TypeSpec.classBuilder(fragmentInfo.screenName!!)
                .superclass(fragmentScreenClass)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            val getFragmentMethod: FunSpec.Builder = FunSpec
                .builder("getFragment")
                .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .addAnnotation(Override::class.java)
                .returns(fragmentInfo.className!!)
            var paramsString = ""
            for (i in fragmentInfo.fragmentParameters.indices) {
                val isLast = i == fragmentInfo.fragmentParameters.size - 1
                val paramInfo = fragmentInfo.fragmentParameters[i]
                val parameterType: TypeName = paramInfo.type
                val parameterName = paramInfo.name
                screenConstructor.addParameter(parameterName, parameterType)
                screenConstructor.addStatement("this.%L = %L", parameterName, parameterName)
                screenClass.addProperty(parameterName, parameterType, KModifier.PRIVATE)
                paramsString = paramsString + if (isLast) parameterName else "$parameterName, "
            }
            getFragmentMethod.addStatement("return %T.newInstance($paramsString)", fragmentInfo.mappedBindingType!!)
            screenClass.addFunction(screenConstructor.build())
            screenClass.addFunction(getFragmentMethod.build())
            groupClass.addType(screenClass.build())
        })
        return groupClass.build()
    }
}