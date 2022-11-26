package com.github.rooneyandshadows.lightbulb.annotation_processors.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.*
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup
import com.squareup.kotlinpoet.*
import java.io.IOException
import java.util.function.Consumer
import javax.annotation.processing.Filer

class CodeGenerator(rootPackage: String, private val filer: Filer) {
    private val routingPackage: String
    private val screensPackage: String
    private val screensClassName: ClassName

    init {
        routingPackage = "$rootPackage.routing"
        screensPackage = "$routingPackage.screens"
        screensClassName = ClassName(screensPackage, "Screens")
    }

    private fun generateActivityNavigatorSingleton(activityClassName: ClassName, routerClassName: ClassName) {
        val className = activityClassName.simpleName.plus("Navigator")
        val file = FileSpec.builder(activityClassName.packageName, className)
        val navigatorClassName = ClassName("", className)
        val singletonClass: TypeSpec.Builder = TypeSpec.objectBuilder(navigatorClassName)
            .addModifiers(KModifier.PUBLIC)
            .addProperty(
                PropertySpec.builder("router", routerClassName.copy(true), KModifier.PRIVATE)
                    .mutable(true)
                    .initializer("null")
                    .build()
            )
            .addFunction(
                FunSpec.builder("route")
                    .addAnnotation(JvmStatic::class.java)
                    .addModifiers(KModifier.PUBLIC)
                    .returns(routerClassName)
                    .addStatement("return router!!")
                    .build()
            ).addFunction(
                FunSpec.builder("initializeRouter")
                    .addParameter("activity", BASE_ACTIVITY)
                    .addParameter("fragmentContainerId", Int::class.javaPrimitiveType!!)
                    .returns(routerClassName)
                    .addStatement("this.router = %T(%L,%L)", routerClassName, "activity", "fragmentContainerId")
                    .addStatement("return this.router!!")
                    .build()
            ).addFunction(
                FunSpec.builder("unBind")
                    .addStatement("this.router = null")
                    .build()
            )
        file.addType(singletonClass.build())
        file.build().writeTo(filer)
    }

    fun generateRouterClass(activityClassName: ClassName, screenGroups: List<FragmentScreenGroup>) {
        val fileName = activityClassName.simpleName.plus("Router")
        val routerPackage: String = activityClassName.packageName
        val file = FileSpec.builder(routerPackage, fileName)
        val routerClassName = ClassName(routerPackage, fileName)
        val routerClass: TypeSpec.Builder = TypeSpec.classBuilder(routerClassName)
            .superclass(BASE_ROUTER)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            .addFunction(
                FunSpec.constructorBuilder()
                    .callSuperConstructor("contextActivity", "fragmentContainerId")
                    .addModifiers(KModifier.PUBLIC)
                    .addParameter("contextActivity", BASE_ACTIVITY)
                    .addParameter("fragmentContainerId", INT)
                    .build()
            )
        screenGroups.forEach(Consumer { group: FragmentScreenGroup ->
            group.screens.forEach { fragment: FragmentInfo ->
                generateRouteClass(
                    routerClass,
                    fragment,
                    group,
                    routerClassName
                )
            }
        })

        try {
            file.addType(routerClass.build())
            file.build().writeTo(filer)
        } catch (e: IOException) {
            //e.printStackTrace();
        }
        generateActivityNavigatorSingleton(activityClassName, routerClassName)
    }

    private fun generateRouteClass(
        routerClass: TypeSpec.Builder,
        fragment: FragmentInfo,
        fragmentGroup: FragmentScreenGroup,
        routerClassName: ClassName
    ) {
        val groupName = fragmentGroup.screenGroupName
        val screenName = fragment.screenName
        val screenClassName = groupName.plus(screenName)
        val groupClass = screensClassName.nestedClass(groupName)
        val screenClass = groupClass.nestedClass(screenName!!)
        val routeClassName = ClassName("", screenClassName)
        val routeClassBuilder = TypeSpec.classBuilder(screenClassName)
            .addModifiers(KModifier.FINAL, KModifier.PUBLIC, KModifier.INNER)
            .addProperty("screen", screenClass)
        val routeClassConstructor = FunSpec.constructorBuilder()
        //.addModifiers(KModifier.PRIVATE)
        val routeMethod = FunSpec.builder("to" + routeClassName.simpleName)
            .addModifiers(KModifier.PUBLIC)
            .returns(routeClassName)
        var paramsString = ""
        for (i in fragment.fragmentParameters.indices) {
            val isLastParameter = i == fragment.fragmentParameters.size - 1
            val parameter = fragment.fragmentParameters[i]
            val paramType: TypeName = parameter.type
            val paramName = parameter.name
            routeClassConstructor.addParameter(paramName, paramType)
            routeClassConstructor.addStatement("this.%L = %L", paramName, paramName)
            routeClassBuilder.addProperty(paramName, paramType, KModifier.PRIVATE)
            routeMethod.addParameter(paramName, paramType)
            paramsString = paramsString + if (isLastParameter) paramName else "$paramName, "
        }
        routeClassConstructor.addStatement("this.screen = %T(%L)", screenClass, paramsString)
        routeClassBuilder.addFunction(routeClassConstructor.build())
            .addFunction(generateRouteForwardMethodForScreen(routerClassName.simpleName))
            .addFunction(generateRouteReplaceMethodForScreen(routerClassName.simpleName))
            .addFunction(generateRouteBackNTimesAndReplaceMethodForScreen(routerClassName.simpleName))
            .addFunction(generateRouteToNewRootScreenMethodForScreen(routerClassName.simpleName))
        routeMethod.addStatement("return %T(%L)", routeClassName, paramsString)
        routerClass.addFunction(routeMethod.build())
        routerClass.addType(routeClassBuilder.build())
    }

    fun generateRoutingScreens(screenGroups: List<FragmentScreenGroup>) {
        val fileName = "Screens"
        val file = FileSpec.builder(screensPackage, fileName)
        val rootClass = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
        screenGroups.forEach(Consumer { group: FragmentScreenGroup -> rootClass.addType(group.build()) })
        try {
            file.addType(rootClass.build())
            file.build().writeTo(filer)
        } catch (e: IOException) {
            //e.printStackTrace();
        }
    }

    fun generateFragmentBindingClasses(fragmentInfoList: List<FragmentInfo>) {
        val methods: MutableList<FunSpec> = mutableListOf()
        fragmentInfoList.forEach(Consumer { fragmentInfo: FragmentInfo ->
            methods.clear()
            val className: String = fragmentInfo.className!!.simpleName.plus("Bindings")
            val file = FileSpec.builder(fragmentInfo.className!!.packageName, className)
            if (fragmentInfo.isCanBeInstantiated) {
                if (hasOptionalParameters(fragmentInfo)) methods.add(
                    generateFragmentNewInstanceMethod(
                        fragmentInfo,
                        false
                    )
                )
                methods.add(generateFragmentNewInstanceMethod(fragmentInfo, true))
            }
            val fragConfigMethod = generateFragmentConfigurationMethod(fragmentInfo)
            if (fragConfigMethod != null) methods.add(fragConfigMethod)
            methods.add(generateFragmentViewBindingsMethod(fragmentInfo))
            methods.add(generateFragmentParametersMethod(fragmentInfo))
            methods.add(generateSaveVariablesMethod(fragmentInfo))
            methods.add(generateRestoreVariablesMethod(fragmentInfo))
            val generatedClass = TypeSpec.classBuilder(className)
                .addModifiers(KModifier.INTERNAL, KModifier.FINAL)
            val companion = TypeSpec.companionObjectBuilder()
                .addFunctions(methods)
            generatedClass.addType(companion.build())
            fragmentInfo.mappedBindingType = generateMappedFragmentBindingClassName(fragmentInfo, className)
            try {
                file.addType(generatedClass.build())
                    .build()
                    .writeTo(filer)
            } catch (e: IOException) {
                //e.printStackTrace();
            }
        })
    }

    private fun generateRouteToNewRootScreenMethodForScreen(routerClassName: String): FunSpec {
        val methodName = "newRootScreen"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
        methodBuilder.addStatement("%L.newRootScreen(screen)", "this@$routerClassName")
        return methodBuilder.build()
    }

    private fun generateRouteBackNTimesAndReplaceMethodForScreen(routerClassName: String): FunSpec {
        val methodName = "backAndReplace"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("backNTimes", INT)
        methodBuilder.addStatement("%L.backNTimesAndReplace(backNTimes,screen)", "this@$routerClassName")
        return methodBuilder.build()
    }

    private fun generateRouteReplaceMethodForScreen(routerClassName: String): FunSpec {
        val methodName = "replace"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
        methodBuilder.addStatement("%L.replaceTop(screen)", "this@$routerClassName")
        return methodBuilder.build()
    }

    private fun generateRouteForwardMethodForScreen(routerClassName: String): FunSpec {
        val methodName = "forward"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
        methodBuilder.addStatement("%L.forward(screen)", "this@$routerClassName")
        return methodBuilder.build()
    }

    private fun generateFragmentConfigurationMethod(fragment: FragmentInfo): FunSpec? {
        val hasFragmentConfigAnnotation = fragment.configAnnotation != null
        if (!hasFragmentConfigAnnotation) return null
        val layoutName: String = fragment.configAnnotation!!.layoutName
        val isMainScreenFragment: String = fragment.configAnnotation!!.isMainScreenFragment.toString()
        val hasLeftDrawer: String = fragment.configAnnotation!!.hasLeftDrawer.toString()
        val hasOptionsMenu: String = fragment.configAnnotation!!.hasOptionsMenu.toString()
        return FunSpec.builder("generateConfiguration")
            .addAnnotation(JvmStatic::class.java)
            .addModifiers(KModifier.INTERNAL)
            .addParameter("fragment", fragment.className!!)
            .returns(BASE_FRAGMENT_CONFIGURATION)
            .addStatement(
                "val layoutId = fragment.getResources().getIdentifier(%S, %S, fragment.requireActivity().getPackageName())",
                layoutName,
                "layout"
            )
            .addStatement(
                "return %T(%L,%L,%L,%L)",
                BASE_FRAGMENT_CONFIGURATION,
                "layoutId",
                isMainScreenFragment,
                hasLeftDrawer,
                hasOptionsMenu
            )
            .build()
    }

    private fun generateFragmentViewBindingsMethod(fragment: FragmentInfo): FunSpec {
        val method = FunSpec.builder("generateViewBindings")
            .addParameter("fragment", fragment.className!!)
            .addAnnotation(JvmStatic::class.java)
            .addModifiers(KModifier.INTERNAL)
        fragment.viewBindings.forEach { (fieldName: String?, identifierName: String?) ->
            val statement =
                "fragment.%L = fragment.requireView().findViewById(fragment.getResources().getIdentifier(%S, %S, fragment.requireActivity().getPackageName()))"
            method.addStatement(statement, fieldName, identifierName, "id")
        }
        return method.build()
    }

    private fun generateFragmentParametersMethod(fragment: FragmentInfo): FunSpec {
        val method = FunSpec.builder("generateParameters")
            .addParameter("fragment", fragment.className!!)
            .addAnnotation(JvmStatic::class.java)
            .addModifiers(KModifier.INTERNAL)
        method.addStatement("val arguments = fragment.requireArguments()")
        fragment.fragmentParameters.forEach(Consumer { param: FragmentParamInfo ->
            val readStatement = resolveReadParamFromBundleExpression(
                "fragment",
                param,
                "arguments",
                true
            )
            method.addCode(readStatement)
        })
        return method.build()
    }

    private fun generateFragmentNewInstanceMethod(
        fragmentInfo: FragmentInfo,
        includeOptionalParams: Boolean
    ): FunSpec {
        val method = FunSpec.builder("newInstance")
        method
            .addAnnotation(JvmStatic::class.java)
            .addModifiers(KModifier.INTERNAL)
            .returns(fragmentInfo.className!!)
            .addStatement("val fragment = %T()", fragmentInfo.className!!)
            .addStatement("val arguments = %T()", BUNDLE)
        fragmentInfo.getNotOptionalParameters().forEach { param ->
            method.addParameter(
                param.name,
                param.type
            )
            val writeStatement = generateNewInstanceBlockOfParam(
                param,
                "arguments"
            )
            method.addCode(writeStatement)
        }
        if (includeOptionalParams)
            fragmentInfo.getOptionalParameters().forEach { param ->
                val nullableType = param.type.copy(true)
                method.addParameter(
                    param.name,
                    nullableType
                )
                val writeStatement = generateNewInstanceBlockOfParam(
                    param,
                    "arguments"
                )
                method.addCode(writeStatement)
            }
        method.addStatement("fragment.setArguments(arguments)")
        method.addStatement("return fragment")
        return method.build()
    }

    private fun generateSaveVariablesMethod(fragmentInfo: FragmentInfo): FunSpec {
        val method = FunSpec
            .builder("saveVariablesState")
            .addAnnotation(JvmStatic::class.java)
            .addModifiers(KModifier.INTERNAL)
            .addParameter("outState", BUNDLE)
            .addParameter("fragment", fragmentInfo.className!!)
        fragmentInfo.fragmentParameters.forEach(Consumer { param: FragmentParamInfo ->
            val writeStatement = generateSaveInstanceStateBlockOfParam(
                "fragment",
                "outState",
                param
            )
            method.addCode(writeStatement)
        })
        return method.build()
    }

    private fun generateRestoreVariablesMethod(fragmentInfo: FragmentInfo): FunSpec {
        val method = FunSpec
            .builder("restoreVariablesState")
            .addModifiers(KModifier.INTERNAL)
            .addParameter("fragmentSavedInstanceState", BUNDLE)
            .addParameter("fragment", fragmentInfo.className!!)
        fragmentInfo.fragmentParameters.forEach(Consumer { param: FragmentParamInfo ->
            val readStatement = resolveReadParamFromBundleExpression(
                "fragment",
                param,
                "fragmentSavedInstanceState",
                false
            )
            method.addCode(readStatement)
        })
        return method.build()
    }

    private fun hasOptionalParameters(fragment: FragmentInfo): Boolean {
        for (param in fragment.fragmentParameters) if (param.isOptional) return true
        return false
    }

    private fun resolveReadParamFromBundleExpression(
        fragmentVariableName: String,
        param: FragmentParamInfo,
        bundleVariableName: String,
        validateParameters: Boolean
    ): CodeBlock {
        val typeString = param.type.copy(false).toString()
        val parameterName = param.name
        val isNullable = param.type.isNullable || param.isOptional
        val needsValidation = validateParameters && (!isNullable)
        val codeBlock = CodeBlock.builder()
        if (needsValidation)
            addBundleEntryValidationExpression(bundleVariableName, parameterName, codeBlock)
        else
            codeBlock.beginControlFlow("if($bundleVariableName.containsKey(%S))", parameterName)
        if (isSimpleType(typeString)) {
            if (typeString == String::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getString(%S)?:\"\"",
                    parameterName,
                    parameterName
                )
            } else if (typeString == uuidType) {
                codeBlock.addStatement(
                    "val %L = %T.fromString($bundleVariableName.getString(%S)?:\"\")",
                    parameterName,
                    UUID,
                    parameterName
                )
            } else if (typeString == Int::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getInt(%S)",
                    parameterName,
                    parameterName
                )
            } else if (typeString == Boolean::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getBoolean(%S)",
                    parameterName,
                    parameterName
                )
            } else if (typeString == Float::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getFloat(%S)",
                    parameterName,
                    parameterName
                )
            } else if (typeString == Long::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getLong(%S)",
                    parameterName,
                    parameterName
                )
            } else if (typeString == Double::class.qualifiedName) {
                codeBlock.addStatement(
                    "val %L = $bundleVariableName.getDouble(%S)",
                    parameterName,
                    parameterName
                )
            }

        } else if (typeString == dateType || typeString == OffsetDateType) {
            codeBlock.addStatement(
                "val %LDateString = $bundleVariableName.getString(%S)",
                parameterName,
                parameterName
            )
            codeBlock.addStatement(
                "val %L = %T.getDateFromStringInDefaultFormat(%L)",
                parameterName,
                if (typeString == dateType) DATE_UTILS else OFFSET_DATE_UTILS,
                parameterName + "DateString"
            )
            val errorString = "Argument $parameterName is provided but date could not be parsed."
            codeBlock.beginControlFlow("if($parameterName == null)")
                .addStatement("throw java.lang.IllegalArgumentException(%S)", errorString)
                .endControlFlow()
        } else {
            val bundleExp = "val %L = $bundleVariableName.getParcelable(%S)"
            codeBlock.addStatement(bundleExp, parameterName, parameterName)
            if (needsValidation) addBundleEntryValidationExpression(bundleVariableName, parameterName, codeBlock)
        }
        codeBlock.addStatement("%L.%L = %L", fragmentVariableName, parameterName, parameterName)
        if (!needsValidation) codeBlock.endControlFlow()
        return codeBlock.build()
    }

    private fun generateSaveInstanceStateBlockOfParam(
        fragmentVariableName: String,
        bundleVariableName: String,
        param: FragmentParamInfo
    ): CodeBlock {
        val paramName = fragmentVariableName.plus(".").plus(param.name)
        val typeString = param.type.copy(false).toString()
        val codeBlock = CodeBlock.builder()
        val isNullable = param.type.isNullable || param.isOptional
        if (isNullable) codeBlock.beginControlFlow("if($paramName != null)")
        if (typeString == String::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == uuidType) {
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L.toString())",
                param.name,
                paramName
            )
        } else if (typeString == Int::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putInt(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == Boolean::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putBoolean(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == Float::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putFloat(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == Long::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putLong(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == Double::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putDouble(%S,%L)",
                param.name,
                paramName
            )
        } else if (typeString == dateType) {
            codeBlock.addStatement(
                "val %LDateString = %T.getDateStringInDefaultFormat(%L)",
                param.name,
                DATE_UTILS,
                paramName
            )
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                param.name,
                param.name + "DateString"
            )
        } else if (typeString == OffsetDateType) {
            codeBlock.addStatement(
                "val %LDateString = %T.getDateStringInDefaultFormat(%L)",
                param.name,
                OFFSET_DATE_UTILS,
                paramName
            )
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                param.name,
                param.name + "DateString"
            )
        } else {
            codeBlock.addStatement(
                "$bundleVariableName.putParcelable(%S,%L)",
                param.name,
                paramName
            )
        }
        if (isNullable) codeBlock.endControlFlow()
        return codeBlock.build()
    }

    private fun generateNewInstanceBlockOfParam(
        param: FragmentParamInfo,
        bundleVariableName: String,
    ): CodeBlock {
        val paramName = param.name
        val typeString = param.type.copy(false).toString()
        val codeBlock = CodeBlock.builder()
        val isNullable = param.type.isNullable || param.isOptional
        if (isNullable)
            codeBlock.beginControlFlow("if($paramName != null)")
        if (typeString == String::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                paramName,
                paramName
            )
        } else if (typeString == uuidType) {
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L.toString())",
                paramName,
                paramName
            )
        } else if (typeString == Int::class.qualifiedName) {
            codeBlock.addStatement("$bundleVariableName.putInt(%S,%L)", paramName, paramName)
        } else if (typeString == Boolean::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putBoolean(%S,%L)",
                paramName,
                paramName
            )
        } else if (typeString == Float::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putFloat(%S,%L)",
                paramName,
                paramName
            )
        } else if (typeString == Long::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putLong(%S,%L)",
                paramName,
                paramName
            )
        } else if (typeString == Double::class.qualifiedName) {
            codeBlock.addStatement(
                "$bundleVariableName.putDouble(%S,%L)",
                paramName,
                paramName
            )
        } else if (typeString == dateType) {
            codeBlock.addStatement(
                "var %LDateString = %T.getDateStringInDefaultFormat(%L)",
                paramName,
                DATE_UTILS,
                paramName
            )
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                paramName,
                paramName + "DateString"
            )
        } else if (typeString == OffsetDateType) {
            codeBlock.addStatement(
                "var %LDateString = %T.getDateStringInDefaultFormat(%L)",
                paramName,
                OFFSET_DATE_UTILS,
                paramName
            )
            codeBlock.addStatement(
                "$bundleVariableName.putString(%S,%L)",
                paramName,
                paramName + "DateString"
            )
        } else {
            codeBlock.addStatement(
                "$bundleVariableName.putParcelable(%S,%L)",
                paramName,
                paramName
            )
        }
        if (isNullable)
            codeBlock.endControlFlow()
        return codeBlock.build()
    }

    private fun addBundleEntryValidationExpression(
        bundleVariableName: String,
        paramName: String,
        codeBlock: CodeBlock.Builder
    ) {
        val errorMessage = "Argument $paramName is not optional."
        codeBlock.beginControlFlow("if(!$bundleVariableName.containsKey(%S))", paramName)
            .addStatement("throw java.lang.IllegalArgumentException(%S)", errorMessage)
            .endControlFlow()
    }
}