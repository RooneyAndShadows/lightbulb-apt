package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentVariableInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.TypeUtils.*;

@SuppressWarnings("DuplicatedCode")
public class CodeGenerator {
    private final Filer filer;
    private final String routingPackage;
    private final String screensPackage;
    private final ClassName screensClassName;

    public CodeGenerator(String rootPackage, Filer filer) {
        this.filer = filer;
        routingPackage = rootPackage.concat(".routing");
        screensPackage = routingPackage.concat(".screens");
        screensClassName = ClassName.get(screensPackage, "Screens");
    }

    private void generateActivityNavigatorSingleton(ClassName activityClassName, ClassName routerClassName) {
        ClassName navigatorClassName = ClassName.get("", activityClassName.simpleName().concat("Navigator"));
        String navigatorPackage = activityClassName.packageName();
        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(navigatorClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(navigatorClassName, "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addField(routerClassName, "router", Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .returns(navigatorClassName)
                        .beginControlFlow("if(instance == null)")
                        .addStatement("instance = new $T()", navigatorClassName)
                        .endControlFlow()
                        .addStatement("return instance")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("getRouter")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(routerClassName)
                        .addStatement("return router")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("route")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(routerClassName)
                        .addStatement("return getInstance().getRouter()")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("initializeRouter")
                        .addParameter(BASE_ACTIVITY, "activity")
                        .addParameter(int.class, "fragmentContainerId")
                        .returns(routerClassName)
                        .addStatement("this.router = new $T($L,$L)", routerClassName, "activity", "fragmentContainerId")
                        .addStatement("return this.router")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("unBind")
                        .returns(void.class)
                        .addStatement("this.router = null")
                        .build()
                );
        try {
            JavaFile.builder(navigatorPackage, singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void generateRouterClass(ClassName activityClassName, List<FragmentScreenGroup> screenGroups) {
        String routerPackage = activityClassName.packageName();
        ClassName routerClassName = ClassName.get(routerPackage, activityClassName.simpleName().concat("Router"));
        TypeSpec.Builder routerClass = TypeSpec
                .classBuilder(routerClassName)
                .superclass(BASE_ROUTER)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(BASE_ACTIVITY, "contextActivity")
                        .addParameter(TypeName.INT, "fragmentContainerId")
                        .addStatement("super(contextActivity,fragmentContainerId)")
                        .build()
                );

        screenGroups.forEach(group -> {
            group.getScreens().forEach(fragment -> {
                generateRouteClass(routerClass, fragment, group, routerClassName);
            });
        });
        try {
            JavaFile.builder(routerPackage, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        generateActivityNavigatorSingleton(activityClassName, routerClassName);
    }

    private void generateRouteClass(TypeSpec.Builder routerClass, FragmentInfo fragment, FragmentScreenGroup fragmentGroup, ClassName routerClassName) {
        boolean hasOptionalParameters = fragment.hasOptionalParameters();
        String groupName = fragmentGroup.getScreenGroupName();
        String screenName = fragment.getScreenName();
        String screenClassName = groupName.concat(screenName);
        ClassName groupClass = screensClassName.nestedClass(groupName);
        ClassName screenClass = groupClass.nestedClass(screenName);
        ClassName routeClassName = ClassName.get("", screenClassName);
        TypeSpec.Builder routeClassBuilder = TypeSpec.classBuilder(screenClassName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);
        MethodSpec.Builder notOptionalConstructor = !hasOptionalParameters ? null : MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        MethodSpec.Builder optionalConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        MethodSpec.Builder allParamsRouteMethod = MethodSpec.methodBuilder("to".concat(routeClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(routeClassName);
        MethodSpec.Builder requiredParamsRouteMethod = !hasOptionalParameters ? null : MethodSpec
                .methodBuilder("to".concat(routeClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(routeClassName);
        String allParams = fragment.generateCommaSeparatedParams(true, parameter -> {
            optionalConstructor.addParameter(parameter.getParameterSpec());
            allParamsRouteMethod.addParameter(parameter.getParameterSpec());
        });
        allParamsRouteMethod.addStatement("return new $T($L)", routeClassName, allParams);
        optionalConstructor.addStatement("this.screen = new $T($L)", screenClass, allParams);
        if (notOptionalConstructor != null) {
            String notOptionalParams = fragment.generateCommaSeparatedParams(false, parameter -> {
                notOptionalConstructor.addParameter(parameter.getParameterSpec());
                requiredParamsRouteMethod.addParameter(parameter.getParameterSpec());
            });
            notOptionalConstructor.addStatement("this.screen = new $T($L)", screenClass, notOptionalParams);
            requiredParamsRouteMethod.addStatement("return new $T($L)", routeClassName, notOptionalParams);
        }
        routeClassBuilder.addField(screenClass, "screen", Modifier.PRIVATE, Modifier.FINAL);
        routeClassBuilder.addMethod(optionalConstructor.build())
                .addMethod(generateRouteForwardMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteReplaceMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteBackNTimesAndReplaceMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteToNewRootScreenMethodForScreen(routerClassName.simpleName()));
        if (hasOptionalParameters)
            routeClassBuilder.addMethod(notOptionalConstructor.build());
        routerClass.addMethod(allParamsRouteMethod.build());
        if (hasOptionalParameters)
            routerClass.addMethod(requiredParamsRouteMethod.build());
        routerClass.addType(routeClassBuilder.build());
    }


    public void generateRoutingScreens(List<FragmentScreenGroup> screenGroups) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder("Screens")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        screenGroups.forEach(group -> rootClass.addType(group.build()));
        try {
            JavaFile.builder(screensPackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void generateFragmentBindingClasses(List<FragmentInfo> fragmentInfoList) {
        List<MethodSpec> methods = new ArrayList<>();
        fragmentInfoList.forEach(fragmentInfo -> {
            methods.clear();
            String className = fragmentInfo.getClassName().simpleName().concat("Bindings");
            if (fragmentInfo.isCanBeInstantiated()) {
                if (fragmentInfo.hasOptionalParameters())
                    methods.add(generateFragmentNewInstanceMethod(fragmentInfo, false));
                methods.add(generateFragmentNewInstanceMethod(fragmentInfo, true));
            }
            methods.add(generateFragmentConfigurationMethod(fragmentInfo));
            methods.add(generateFragmentViewBindingsMethod(fragmentInfo));
            methods.add(generateFragmentParametersMethod(fragmentInfo));
            methods.add(generateSaveVariablesMethod(fragmentInfo));
            methods.add(generateRestoreVariablesMethod(fragmentInfo));
            TypeSpec.Builder generatedClass = TypeSpec
                    .classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methods);
            fragmentInfo.setMappedBindingType(generateMappedFragmentBindingClassName(fragmentInfo, className));
            try {
                JavaFile.builder(fragmentInfo.getClassName().packageName(), generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private MethodSpec generateRouteToNewRootScreenMethodForScreen(String routerClassName) {
        String methodName = "newRootScreen";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.newRootScreen(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteBackNTimesAndReplaceMethodForScreen(String routerClassName) {
        String methodName = "backAndReplace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.INT, "backNTimes");
        methodBuilder.addStatement("$L.backNTimesAndReplace(backNTimes,screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteReplaceMethodForScreen(String routerClassName) {
        String methodName = "replace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.replaceTop(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteForwardMethodForScreen(String routerClassName) {
        String methodName = "forward";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.forward(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateFragmentConfigurationMethod(FragmentInfo fragment) {
        String methodName = "generateConfiguration";
        boolean hasFragmentConfigAnnotation = fragment.getConfigAnnotation() != null;
        if (!hasFragmentConfigAnnotation) {
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(fragment.getClassName(), "fragment")
                    .returns(BASE_FRAGMENT_CONFIGURATION)
                    .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, -1, true, false, false)
                    .build();
        }
        String layoutName = fragment.getConfigAnnotation().layoutName();
        String isMainScreenFragment = String.valueOf(fragment.getConfigAnnotation().isMainScreenFragment());
        String hasLeftDrawer = String.valueOf(fragment.getConfigAnnotation().hasLeftDrawer());
        String hasOptionsMenu = String.valueOf(fragment.getConfigAnnotation().hasOptionsMenu());
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(fragment.getClassName(), "fragment")
                .returns(BASE_FRAGMENT_CONFIGURATION)
                .addStatement("int layoutId = fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName())", layoutName, "layout")
                .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, "layoutId", isMainScreenFragment, hasLeftDrawer, hasOptionsMenu)
                .build();
    }

    private MethodSpec generateFragmentViewBindingsMethod(FragmentInfo fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateViewBindings")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        fragment.getViewBindings().forEach(bindingInfo -> {
            String fieldName = bindingInfo.getFieldName();
            String resourceName = bindingInfo.getResourceName();
            if (bindingInfo.hasSetter()) {
                String statement = "fragment.$L(fragment.getView().findViewById(fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName())))";
                method.addStatement(statement, bindingInfo.getSetterName(), resourceName, "id");
            } else {
                String statement = "fragment.$L = fragment.getView().findViewById(fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName()))";
                method.addStatement(statement, fieldName, resourceName, "id");
            }
        });
        return method.build();
    }

    private MethodSpec generateFragmentParametersMethod(FragmentInfo fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateParameters")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        method.addStatement("$T arguments = fragment.getArguments()", BUNDLE);
        fragment.getFragmentParameters().forEach(param -> {
            CodeBlock readStatement = resolveReadParamFromBundleExpression(
                    "fragment",
                    param,
                    "arguments",
                    true
            );
            method.addCode(readStatement);
        });
        return method.build();
    }

    private MethodSpec generateFragmentNewInstanceMethod(FragmentInfo fragmentInfo, boolean includeOptionalParams) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("newInstance");
        method.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T arguments = new $T()", BUNDLE, BUNDLE);
        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(param -> {
            method.addParameter(param.getParameterSpec());
            CodeBlock writeStatement = generateNewInstanceBlockOfParam(param, "arguments");
            method.addCode(writeStatement);
        });
        method.addStatement("fragment.setArguments(arguments)");
        method.addStatement("return fragment");
        return method.build();
    }

    private MethodSpec generateSaveVariablesMethod(FragmentInfo fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BUNDLE, "outState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getFragmentParameters().forEach(param -> {
            CodeBlock writeStatement = generateSaveInstanceStateBlockOfParam(
                    param,
                    "fragment",
                    "outState"
            );
            method.addCode(writeStatement);
        });
        fragmentInfo.getFragmentPersistedVariables().forEach(param -> {
            CodeBlock writeStatement = generateSaveInstanceStateBlockOfParam(
                    param,
                    "fragment",
                    "outState"
            );
            method.addCode(writeStatement);
        });
        return method.build();
    }

    private MethodSpec generateRestoreVariablesMethod(FragmentInfo fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BUNDLE, "fragmentSavedInstanceState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getFragmentParameters().forEach(param -> {
            CodeBlock readStatement = resolveReadParamFromBundleExpression(
                    "fragment",
                    param,
                    "fragmentSavedInstanceState",
                    false
            );
            method.addCode(readStatement);
        });
        fragmentInfo.getFragmentPersistedVariables().forEach(param -> {
            CodeBlock readStatement = resolveReadParamFromBundleExpression(
                    "fragment",
                    param,
                    "fragmentSavedInstanceState",
                    false
            );
            method.addCode(readStatement);
        });
        return method.build();
    }

    @SuppressWarnings("DuplicatedCode")
    private CodeBlock resolveReadParamFromBundleExpression(String fragmentVariableName, FragmentVariableInfo parameter, String bundleVariableName, boolean validateParameters) {
        String typeString = parameter.getType().toString();
        TypeName paramType = parameter.getType();
        String parameterName = parameter.getName();
        boolean isNullable = parameter.isNullable() || ((parameter instanceof FragmentParamInfo) && ((FragmentParamInfo) parameter).isOptional());
        boolean isPrimitive = parameter.getType().isPrimitive();
        boolean needsValidation = !isPrimitive && validateParameters && !isNullable;
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        if (needsValidation) addBundleEntryValidationExpression(bundleVariableName, parameterName, codeBlock);
        else codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, parameterName);
        if (isSimpleType(typeString)) {
            if (isString(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getString($S,\"\")", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isUUID(typeString)) {
                String bundleExp = String.format("$T $L = $T.fromString(%s.getString($S,\"\"))", bundleVariableName);
                codeBlock.addStatement(bundleExp, UUID, parameterName, UUID, parameterName);
            } else if (isInt(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getInt($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isBoolean(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getBoolean($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isFloat(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getFloat($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isLong(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getLong($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isDouble(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getDouble($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            }
        } else if (isDate(typeString)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.addStatement("$T $LDateString = ".concat(getDateStringExpression).concat(""), STRING, parameterName, parameterName);
            codeBlock.addStatement("$T $L = $T.getDateFromString($T.$L,$L)", DATE, parameterName, DATE_UTILS, DATE_UTILS, "defaultFormat", parameterName.concat("DateString"));
            if (validateParameters) {
                String errorString = String.format("Argument %s is provided but date could not be parsed.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        } else if (isOffsetDate(typeString)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.addStatement("$T $LDateString = ".concat(getDateStringExpression).concat(""), STRING, parameterName, parameterName);
            codeBlock.addStatement("$T $L = $T.getDateFromString($T.$L,$L)", OFFSET_DATE_TIME, parameterName, OFFSET_DATE_UTILS, OFFSET_DATE_UTILS, "defaultFormatWithTimeZone", parameterName.concat("DateString"));
            if (validateParameters) {
                String errorString = String.format("Argument %s is provided but date could not be parsed.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        } else {
            codeBlock.addStatement("$T $L", paramType, parameterName);
            codeBlock.beginControlFlow("if($L >= $L)", SDK_INT, ClassNames.generateVersionCodeClassName("TIRAMISU"))
                    .addStatement("$L = ".concat(String.format("%s.getParcelable($S,$L)", bundleVariableName)), parameterName, parameterName, paramType.toString().concat(".class"))
                    .nextControlFlow("else")
                    .addStatement("$L = ".concat(String.format("%s.getParcelable($S)", bundleVariableName)), parameterName, parameterName)
                    .endControlFlow();
            if (validateParameters) {
                String errorString = String.format("Argument %s is not nullable, but null value received from bundle.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        }
        if (parameter.hasSetter())
            codeBlock.addStatement("$L.$L($L)", fragmentVariableName, parameter.getSetterName(), parameterName);
        else
            codeBlock.addStatement("$L.$L = $L", fragmentVariableName, parameterName, parameterName);
        if (!needsValidation) codeBlock.endControlFlow();
        return codeBlock.build();
    }

    private CodeBlock generateSaveInstanceStateBlockOfParam(FragmentVariableInfo parameter, String fragmentVariableName, String bundleVariableName) {
        String typeString = parameter.getType().toString();
        String parameterName = parameter.getName();
        String getExpression = parameter.hasGetter() ? parameter.getGetterName().concat("()") : parameterName;
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        boolean checkForNull = !parameter.getType().isPrimitive();
        if (checkForNull)
            codeBlock.beginControlFlow("if($L.$L != null)", fragmentVariableName, getExpression);
        if (isString(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isUUID(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L.$L.toString())", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isInt(typeString)) {
            codeBlock.addStatement(String.format("%s.putInt($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isBoolean(typeString)) {
            codeBlock.addStatement(String.format("%s.putBoolean($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isFloat(typeString)) {
            codeBlock.addStatement(String.format("%s.putFloat($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isLong(typeString)) {
            codeBlock.addStatement(String.format("%s.putLong($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isDouble(typeString)) {
            codeBlock.addStatement(String.format("%s.putDouble($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        } else if (isDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L.$L);", STRING, parameterName, DATE_UTILS, DATE_UTILS, "defaultFormat", fragmentVariableName, getExpression);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterName, parameterName.concat("DateString"));
        } else if (isOffsetDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L.$L);", STRING, parameterName, OFFSET_DATE_UTILS, OFFSET_DATE_UTILS, "defaultFormatWithTimeZone", fragmentVariableName, getExpression);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterName, parameterName.concat("DateString"));
        } else {
            codeBlock.addStatement(String.format("%s.putParcelable($S,$L.$L)", bundleVariableName), parameterName, fragmentVariableName, getExpression);
        }
        if (checkForNull)
            codeBlock.endControlFlow();
        return codeBlock.build();
    }

    private CodeBlock generateNewInstanceBlockOfParam(FragmentParamInfo parameter, String bundleVariableName) {
        String typeString = parameter.getType().toString();
        String parameterName = parameter.getName();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        boolean needsNullCheck = !parameter.getType().isPrimitive() || parameter.isNullable() || parameter.isOptional();
        if (needsNullCheck)
            codeBlock.beginControlFlow("if($L != null)", parameterName);
        if (isString(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isUUID(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L.toString())", bundleVariableName), parameterName, parameterName);
        } else if (isInt(typeString)) {
            codeBlock.addStatement(String.format("%s.putInt($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isBoolean(typeString)) {
            codeBlock.addStatement(String.format("%s.putBoolean($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isFloat(typeString)) {
            codeBlock.addStatement(String.format("%s.putFloat($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isLong(typeString)) {
            codeBlock.addStatement(String.format("%s.putLong($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isDouble(typeString)) {
            codeBlock.addStatement(String.format("%s.putDouble($S,$L)", bundleVariableName), parameterName, parameterName);
        } else if (isDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L)", STRING, parameterName, DATE_UTILS, DATE_UTILS, "defaultFormat", parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterName, parameterName.concat("DateString"));
        } else if (isOffsetDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L)", STRING, parameterName, OFFSET_DATE_UTILS, OFFSET_DATE_UTILS, "defaultFormatWithTimeZone", parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterName, parameterName.concat("DateString"));
        } else {
            codeBlock.addStatement(String.format("%s.putParcelable($S,$L)", bundleVariableName), parameterName, parameterName);
        }
        if (needsNullCheck)
            codeBlock.endControlFlow();
        return codeBlock.build();
    }

    private void addBundleEntryValidationExpression(
            String bundleVariableName,
            String paramName,
            CodeBlock.Builder codeBlock
    ) {
        String errorMessage = String.format("Argument %s is not optional.", paramName);
        codeBlock.beginControlFlow("if(!$L.containsKey($S))", bundleVariableName, paramName)
                .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorMessage)
                .endControlFlow();
    }
}
