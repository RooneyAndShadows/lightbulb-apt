package com.github.rooneyandshadows.lightbulb.annotation_processors.utils

import com.github.rooneyandshadows.lightbulb.annotation_processors.*
import com.github.rooneyandshadows.lightbulb.annotation_processors.STRING
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
            .addProperty("router", routerClassName, KModifier.PRIVATE)
            .addFunction(
                FunSpec.builder("route")
                    .addAnnotation(JvmStatic::class.java)
                    .addModifiers(KModifier.PUBLIC)
                    .returns(routerClassName)
                    .addStatement("return getInstance().getRouter()")
                    .build()
            ).addFunction(
                FunSpec.builder("initializeRouter")
                    .addParameter("activity", BASE_ACTIVITY)
                    .addParameter("fragmentContainerId", Int::class.javaPrimitiveType!!)
                    .returns(routerClassName)
                    .addStatement("this.router = \$T(\$L,\$L)", routerClassName, "activity", "fragmentContainerId")
                    .addStatement("return this.router")
                    .build()
            ).addFunction(
                FunSpec.builder("unBind")
                    .returns(Void.TYPE)
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
                    .addModifiers(KModifier.PUBLIC)
                    .addParameter("contextActivity", BASE_ACTIVITY)
                    .addParameter("fragmentContainerId", INT)
                    .addStatement("super(contextActivity,fragmentContainerId)")
                    .build()
            )
        screenGroups.forEach(Consumer { group: FragmentScreenGroup ->
            group.screens.forEach(
                Consumer { fragment: FragmentInfo ->
                    generateRouteClass(
                        routerClass,
                        fragment,
                        group,
                        routerClassName
                    )
                })
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
            .addModifiers(KModifier.FINAL, KModifier.PUBLIC)
        val routeClassConstructor = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PRIVATE)
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
            routeClassConstructor.addStatement("this.\$L = \$L", paramName, paramName)
            routeClassBuilder.addProperty(paramName, paramType, KModifier.PRIVATE)
            routeMethod.addParameter(paramName, paramType)
            paramsString = paramsString + if (isLastParameter) paramName else "$paramName, "
        }
        routeClassBuilder
            .addFunction(routeClassConstructor.build())
            .addFunction(generateRouteForwardMethodForScreen(screenClass, routerClassName.simpleName, paramsString))
            .addFunction(generateRouteReplaceMethodForScreen(screenClass, routerClassName.simpleName, paramsString))
            .addFunction(
                generateRouteBackNTimesAndReplaceMethodForScreen(
                    screenClass,
                    routerClassName.simpleName,
                    paramsString
                )
            )
            .addFunction(
                generateRouteToNewRootScreenMethodForScreen(
                    screenClass,
                    routerClassName.simpleName,
                    paramsString
                )
            )
        routeMethod.addStatement("return \$T(\$L)", routeClassName, paramsString)
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
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addFunctions(methods)
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

    private fun generateRouteToNewRootScreenMethodForScreen(
        screenClass: ClassName,
        routerClassName: String,
        paramsString: String
    ): FunSpec {
        val methodName = "newRootScreen"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
        methodBuilder.addStatement(
            "\$L.newRootScreen(\$T(\$L))",
            "$routerClassName.this",
            screenClass,
            paramsString
        )
        return methodBuilder.build()
    }

    private fun generateRouteBackNTimesAndReplaceMethodForScreen(
        screenClass: ClassName,
        routerClassName: String,
        paramsString: String
    ): FunSpec {
        val methodName = "backAndReplace"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
            .addParameter("backNTimes", INT)
        methodBuilder.addStatement(
            "\$L.backNTimesAndReplace(backNTimes,\$T(\$L))",
            "$routerClassName.this",
            screenClass,
            paramsString
        )
        return methodBuilder.build()
    }

    private fun generateRouteReplaceMethodForScreen(
        screenClass: ClassName,
        routerClassName: String,
        paramsString: String
    ): FunSpec {
        val methodName = "replace"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
        methodBuilder.addStatement("\$L.replaceTop(\$T(\$L))", "$routerClassName.this", screenClass, paramsString)
        return methodBuilder.build()
    }

    private fun generateRouteForwardMethodForScreen(
        screenClass: ClassName,
        routerClassName: String,
        paramsString: String
    ): FunSpec {
        val methodName = "forward"
        val methodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
        methodBuilder.addStatement("\$L.forward(\$T(\$L))", "$routerClassName.this", screenClass, paramsString)
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
            .addModifiers(KModifier.PUBLIC)
            .addParameter("fragment", fragment.className!!)
            .returns(BASE_FRAGMENT_CONFIGURATION)
            .addStatement(
                "val layoutId = fragment.getResources().getIdentifier(\$S, \$S, fragment.getActivity().getPackageName())",
                layoutName,
                "layout"
            )
            .addStatement(
                "return \$T(\$L,\$L,\$L,\$L)",
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
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
        fragment.viewBindings.forEach { (fieldName: String?, identifierName: String?) ->
            val statement =
                "fragment.\$L = fragment.getView().findViewById(fragment.getResources().getIdentifier(\$S, \$S, fragment.getActivity().getPackageName()))"
            method.addStatement(statement, fieldName, identifierName, "id")
        }
        return method.build()
    }

    private fun generateFragmentParametersMethod(fragment: FragmentInfo): FunSpec {
        val method = FunSpec.builder("generateParameters")
            .addParameter("fragment", fragment.className!!)
            .addModifiers(KModifier.PUBLIC)
            .returns(Void.TYPE)
        method.addStatement("\$T arguments = fragment.getArguments()", BUNDLE)
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
        method.addModifiers(KModifier.PUBLIC)
            .returns(fragmentInfo.className!!)
            .addStatement("\$T fragment = new \$T()", fragmentInfo.className!!, fragmentInfo.className!!)
            .addStatement("\$T arguments = new \$T()", BUNDLE, BUNDLE)
        fragmentInfo.fragmentParameters.forEach { param ->
            val acceptParam = !param.isOptional || includeOptionalParams
            if (!acceptParam) return@forEach
            method.addParameter(
                param.name,
                param.type
            )
            val writeStatement = generateNewInstanceBlockOfParam(
                param.type,
                param.name,
                param.name,
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
            .addModifiers(KModifier.PUBLIC)
            .addParameter("outState", BUNDLE)
            .addParameter("fragment", fragmentInfo.className!!)
            .returns(Void.TYPE)
        fragmentInfo.fragmentParameters.forEach(Consumer { param: FragmentParamInfo ->
            val writeStatement = generateSaveInstanceStateBlockOfParam(
                "fragment",
                param.type,
                param.name,
                param.name,
                "outState"
            )
            method.addCode(writeStatement)
        })
        return method.build()
    }

    private fun generateRestoreVariablesMethod(fragmentInfo: FragmentInfo): FunSpec {
        val method = FunSpec
            .builder("restoreVariablesState")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("fragmentSavedInstanceState", BUNDLE)
            .addParameter("fragment", fragmentInfo.className!!)
            .returns(Void.TYPE)
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
        val typeString = param.type.toString()
        val paramType: TypeName = param.type
        val parameterName = param.name
        val optional = param.isOptional
        val needsValidation = validateParameters && (!optional)
        val codeBlock = CodeBlock.builder()
        if (isSimpleType(typeString)) {
            if (typeString == stringType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getString(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            } else if (typeString == uuidType) {
                val tmpVariableName = parameterName + "String"
                val bundleExp = "\$T \$L = " + String.format("%s.getString(\$S)", bundleVariableName)
                codeBlock.addStatement("\$T \$L = null", UUID, parameterName)
                codeBlock.addStatement(bundleExp, STRING, tmpVariableName, parameterName)
                codeBlock.beginControlFlow("if(\$L != null)", tmpVariableName)
                    .addStatement("\$L = \$T.fromString(\$L)", parameterName, UUID, tmpVariableName)
                    .endControlFlow()
            } else if (typeString == intType || typeString == intPrimType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getInt(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            } else if (typeString == booleanType || typeString == booleanPrimType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getBoolean(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            } else if (typeString == floatType || typeString == floatPrimType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getFloat(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            } else if (typeString == longType || typeString == longPrimType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getLong(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            } else if (typeString == doubleType || typeString == doublePrimType) {
                val bundleExp = "\$T \$L = " + String.format("%s.getDouble(\$S)", bundleVariableName)
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            }
            if (needsValidation) addValidationExpression(codeBlock, parameterName)
        } else if (typeString == dateType || typeString == OffsetDateType) {
            val getDateStringExpression = String.format("%s.getString(\$S)", bundleVariableName)
            codeBlock.addStatement(
                "\$T \$LDateString = $getDateStringExpression",
                STRING,
                parameterName,
                parameterName
            )
            if (needsValidation) addValidationExpression(codeBlock, parameterName + "DateString")
            codeBlock.addStatement(
                "\$T \$L = \$T.getDateFromStringInDefaultFormat(\$L)",
                if (typeString == dateType) DATE else OFFSET_DATE_TIME,
                parameterName,
                if (typeString == dateType) DATE_UTILS else OFFSET_DATE_UTILS,
                parameterName + "DateString"
            )
            if (needsValidation) addValidationExpression(codeBlock, parameterName)
        } else {
            val bundleExp = "\$T \$L = " + String.format("%s.getParcelable(\$S)", bundleVariableName)
            codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName)
            if (needsValidation) addValidationExpression(codeBlock, parameterName)
        }
        codeBlock.addStatement("\$L.\$L = \$L", fragmentVariableName, parameterName, parameterName)
        return codeBlock.build()
    }

    private fun generateSaveInstanceStateBlockOfParam(
        fragmentVariableName: String,
        type: TypeName,
        parameterKey: String,
        parameterName: String,
        bundleVariableName: String
    ): CodeBlock {
        val typeString = type.toString()
        val codeBlock = CodeBlock.builder()
        if (typeString == stringType) {
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == uuidType) {
            codeBlock.addStatement("\$T \$L = \$S", STRING, parameterName, "")
            codeBlock.beginControlFlow("if(\$L.\$L != null)", fragmentVariableName, parameterName)
                .addStatement("\$L = \$L.\$L.toString()", parameterName, fragmentVariableName, parameterName)
                .endControlFlow()
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == intType || typeString == intPrimType) {
            codeBlock.addStatement(
                String.format("%s.putInt(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == booleanType || typeString == booleanPrimType) {
            codeBlock.addStatement(
                String.format("%s.putBoolean(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == floatType || typeString == floatPrimType) {
            codeBlock.addStatement(
                String.format("%s.putFloat(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == longType || typeString == longPrimType) {
            codeBlock.addStatement(
                String.format("%s.putLong(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == doubleType || typeString == doublePrimType) {
            codeBlock.addStatement(
                String.format("%s.putDouble(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        } else if (typeString == dateType) {
            codeBlock.addStatement(
                "\$T \$LDateString = \$T.getDateStringInDefaultFormat(\$L.\$L);",
                STRING,
                parameterName,
                DATE_UTILS,
                fragmentVariableName,
                parameterName
            )
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName + "DateString"
            )
        } else if (typeString == OffsetDateType) {
            codeBlock.addStatement(
                "\$T \$LDateString = \$T.getDateStringInDefaultFormat(\$L.\$L);",
                STRING,
                parameterName,
                OFFSET_DATE_UTILS,
                fragmentVariableName,
                parameterName
            )
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName + "DateString"
            )
        } else {
            codeBlock.addStatement(
                String.format("%s.putParcelable(\$S,\$L.\$L)", bundleVariableName),
                parameterKey,
                fragmentVariableName,
                parameterName
            )
        }
        return codeBlock.build()
    }

    private fun generateNewInstanceBlockOfParam(
        type: TypeName,
        parameterKey: String,
        parameterName: String,
        bundleVariableName: String
    ): CodeBlock {
        val typeString = type.toString()
        val codeBlock = CodeBlock.builder()
        if (typeString == stringType) {
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == uuidType) {
            val tmpVariableName = parameterName + "String"
            codeBlock.addStatement("\$T \$L = \$S", STRING, tmpVariableName, "")
            codeBlock.beginControlFlow("if(\$L != null)", parameterName)
                .addStatement("\$L = \$L.toString()", tmpVariableName, parameterName)
                .endControlFlow()
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                tmpVariableName
            )
        } else if (typeString == intType || typeString == intPrimType) {
            codeBlock.addStatement(String.format("%s.putInt(\$S,\$L)", bundleVariableName), parameterKey, parameterName)
        } else if (typeString == booleanType || typeString == booleanPrimType) {
            codeBlock.addStatement(
                String.format("%s.putBoolean(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == floatType || typeString == floatPrimType) {
            codeBlock.addStatement(
                String.format("%s.putFloat(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == longType || typeString == longPrimType) {
            codeBlock.addStatement(
                String.format("%s.putLong(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == doubleType || typeString == doublePrimType) {
            codeBlock.addStatement(
                String.format("%s.putDouble(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        } else if (typeString == dateType) {
            codeBlock.addStatement(
                "\$T \$LDateString = \$T.getDateStringInDefaultFormat(\$L)",
                STRING,
                parameterName,
                DATE_UTILS,
                parameterName
            )
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName + "DateString"
            )
        } else if (typeString == OffsetDateType) {
            codeBlock.addStatement(
                "\$T \$LDateString = \$T.getDateStringInDefaultFormat(\$L)",
                STRING,
                parameterName,
                OFFSET_DATE_UTILS,
                parameterName
            )
            codeBlock.addStatement(
                String.format("%s.putString(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName + "DateString"
            )
        } else {
            codeBlock.addStatement(
                String.format("%s.putParcelable(\$S,\$L)", bundleVariableName),
                parameterKey,
                parameterName
            )
        }
        return codeBlock.build()
    }

    private fun addValidationExpression(codeBlock: CodeBlock.Builder, paramName: String) {
        codeBlock.beginControlFlow("if($paramName == null)")
            .addStatement("throw java.lang.IllegalArgumentException(\"Argument $paramName is not optional.\")")
            .endControlFlow()
    }
}