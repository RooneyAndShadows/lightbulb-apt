package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames.GENERATED_LB_SCREENS;

@SuppressWarnings("DuplicatedCode")
public class CodeGenerator {
    private static final String stringType = String.class.getCanonicalName();
    private static final String intType = Integer.class.getCanonicalName();
    private static final String intPrimType = int.class.getCanonicalName();
    private static final String booleanType = Boolean.class.getCanonicalName();
    private static final String booleanPrimType = boolean.class.getCanonicalName();
    private static final String uuidType = UUID.class.getCanonicalName();
    private static final String floatType = Float.class.getCanonicalName();
    private static final String floatPrimType = float.class.getCanonicalName();
    private static final String longType = Long.class.getCanonicalName();
    private static final String longPrimType = long.class.getCanonicalName();
    private static final String doubleType = Double.class.getCanonicalName();
    private static final String doublePrimType = double.class.getCanonicalName();
    private static final String dateType = Date.class.getCanonicalName();
    private static final String OffsetDateType = OffsetDateTime.class.getCanonicalName();
    private static final List<String> simpleTypesList = Arrays.asList(stringType, intType, intPrimType, booleanType,
            booleanPrimType, uuidType, floatType, floatPrimType, longType, longPrimType, doubleType, doublePrimType);

    public static void generateRouterClass(Filer filer, List<FragmentScreenGroup> screenGroups) {
        TypeSpec.Builder routerClass = TypeSpec
                .classBuilder("AppRouter")
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
            String groupName = group.getScreenGroupName();
            group.getScreens().forEach(fragment -> {
                String screenName = fragment.getScreenName();
                ClassName groupClass = GENERATED_SCREENS.nestedClass(groupName);
                ClassName screenClass = groupClass.nestedClass(screenName);
                routerClass.addMethod(generateRouterForwardMethodForScreen(screenClass, fragment, groupName, screenName));
                routerClass.addMethod(generateRouterReplaceMethodForScreen(screenClass, fragment, groupName, screenName));
                routerClass.addMethod(generateRouterBackNTimesAndReplaceMethodForScreen(screenClass, fragment, groupName, screenName));
            });
        });
        try {
            JavaFile.builder(PackageNames.GENERATED_LB_ROUTING, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private static MethodSpec generateRouterBackNTimesAndReplaceMethodForScreen(ClassName screenClass, FragmentInfo fragment, String groupName, String screenName) {
        String methodName = "to" + groupName + screenName + "BackAndReplace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.INT, "backNTimes");
        String paramsString = "";
        for (int i = 0; i < fragment.getFragmentParameters().size(); i++) {
            boolean isLast = i == fragment.getFragmentParameters().size() - 1;
            FragmentParamInfo param = fragment.getFragmentParameters().get(i);
            TypeName paramType = param.getType();
            String paramName = param.getName();
            methodBuilder.addParameter(paramType, paramName);
            paramsString = paramsString.concat(isLast ? paramName : paramName.concat(", "));
        }
        methodBuilder.addStatement("backNTimesAndReplace(backNTimes,new $T($L))", screenClass, paramsString);
        return methodBuilder.build();
    }

    private static MethodSpec generateRouterReplaceMethodForScreen(ClassName screenClass, FragmentInfo fragment, String groupName, String screenName) {
        String methodName = "to" + groupName + screenName + "Replace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        String paramsString = "";
        for (int i = 0; i < fragment.getFragmentParameters().size(); i++) {
            boolean isLast = i == fragment.getFragmentParameters().size() - 1;
            FragmentParamInfo param = fragment.getFragmentParameters().get(i);
            TypeName paramType = param.getType();
            String paramName = param.getName();
            methodBuilder.addParameter(paramType, paramName);
            paramsString = paramsString.concat(isLast ? paramName : paramName.concat(", "));
        }
        methodBuilder.addStatement("replaceTop(new $T($L))", screenClass, paramsString);
        return methodBuilder.build();
    }

    private static MethodSpec generateRouterForwardMethodForScreen(ClassName screenClass, FragmentInfo fragment, String groupName, String screenName) {
        String methodName = "to" + groupName + screenName + "Forward";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        String paramsString = "";
        for (int i = 0; i < fragment.getFragmentParameters().size(); i++) {
            boolean isLast = i == fragment.getFragmentParameters().size() - 1;
            FragmentParamInfo param = fragment.getFragmentParameters().get(i);
            TypeName paramType = param.getType();
            String paramName = param.getName();
            methodBuilder.addParameter(paramType, paramName);
            paramsString = paramsString.concat(isLast ? paramName : paramName.concat(", "));
        }
        methodBuilder.addStatement("forward(new $T($L))", screenClass, paramsString);
        return methodBuilder.build();
    }

    public static void generateRoutingScreens(Filer filer, List<FragmentScreenGroup> screenGroups) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder("Screens")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        screenGroups.forEach(group -> rootClass.addType(group.build()));
        try {
            JavaFile.builder(GENERATED_LB_SCREENS, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public static void generateFragmentBindingClasses(Filer filer, List<FragmentInfo> fragmentInfoList) {
        List<MethodSpec> methods = new ArrayList<>();
        fragmentInfoList.forEach(fragmentInfo -> {
            methods.clear();
            String className = fragmentInfo.getClassName().simpleName().concat("Bindings");
            if (fragmentInfo.isCanBeInstantiated()) {
                if (hasOptionalParameters(fragmentInfo))
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

    private static MethodSpec generateFragmentConfigurationMethod(FragmentInfo fragment) {
        boolean hasFragmentConfigAnnotation = fragment.getConfigAnnotation() != null;
        if (!hasFragmentConfigAnnotation)
            return null;
        String layoutName = fragment.getConfigAnnotation().layoutName();
        String isMainScreenFragment = String.valueOf(fragment.getConfigAnnotation().isMainScreenFragment());
        String hasLeftDrawer = String.valueOf(fragment.getConfigAnnotation().hasLeftDrawer());
        String hasOptionsMenu = String.valueOf(fragment.getConfigAnnotation().hasOptionsMenu());
        return MethodSpec.methodBuilder("generateConfiguration")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BASE_FRAGMENT, "fragment")
                .returns(BASE_FRAGMENT_CONFIGURATION)
                .addStatement("int layoutId = fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName())", layoutName, "layout")
                .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, "layoutId", isMainScreenFragment, hasLeftDrawer, hasOptionsMenu)
                .build();
    }

    private static MethodSpec generateFragmentViewBindingsMethod(FragmentInfo fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateViewBindings")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        fragment.getViewBindings().forEach((fieldName, identifierName) -> {
            String statement = "fragment.$L = fragment.getView().findViewById(fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName()))";
            method.addStatement(statement, fieldName, identifierName, "id");
        });
        return method.build();
    }

    private static MethodSpec generateFragmentParametersMethod(FragmentInfo fragment) {
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

    private static MethodSpec generateFragmentNewInstanceMethod(FragmentInfo fragmentInfo, boolean includeOptionalParams) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("newInstance");
        method.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T arguments = new $T()", BUNDLE, BUNDLE);
        fragmentInfo.getFragmentParameters().forEach(param -> {
            boolean acceptParam = !param.isOptional() || includeOptionalParams;
            if (!acceptParam) return;
            method.addParameter(
                    param.getType(),
                    param.getName()
            );
            CodeBlock writeStatement = generateNewInstanceBlockOfParam(
                    param.getType(),
                    param.getName(),
                    param.getName(),
                    "arguments"
            );
            method.addCode(writeStatement);
        });
        method.addStatement("fragment.setArguments(arguments)");
        method.addStatement("return fragment");
        return method.build();
    }

    private static MethodSpec generateSaveVariablesMethod(FragmentInfo fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BUNDLE, "outState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getFragmentParameters().forEach(param -> {
            CodeBlock writeStatement = generateSaveInstanceStateBlockOfParam(
                    "fragment",
                    param.getType(),
                    param.getName(),
                    param.getName(),
                    "outState"
            );
            method.addCode(writeStatement);
        });
        return method.build();
    }

    private static MethodSpec generateRestoreVariablesMethod(FragmentInfo fragmentInfo) {
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
        return method.build();
    }

    private static boolean hasOptionalParameters(FragmentInfo fragment) {
        for (FragmentParamInfo param : fragment.getFragmentParameters())
            if (param.isOptional()) return true;
        return false;
    }

    @SuppressWarnings("DuplicatedCode")
    private static CodeBlock resolveReadParamFromBundleExpression(String fragmentVariableName, FragmentParamInfo param, String bundleVariableName, boolean validateParameters) {
        String typeString = param.getType().toString();
        TypeName paramType = param.getType();
        String parameterName = param.getName();
        boolean optional = param.isOptional();
        boolean needsValidation = validateParameters && (!optional || !param.getType().isPrimitive());
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        if (simpleTypesList.contains(typeString)) {
            if (typeString.equals(stringType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getString($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (typeString.equals(uuidType)) {
                String tmpVariableName = parameterName.concat("String");
                String bundleExp = "$T $L = ".concat(String.format("%s.getString($S)", bundleVariableName));
                codeBlock.addStatement("$T $L = null", UUID, parameterName);
                codeBlock.addStatement(bundleExp, STRING, tmpVariableName, parameterName);
                codeBlock.beginControlFlow("if($L != null)", tmpVariableName)
                        .addStatement("$L = $T.fromString($L)", parameterName, UUID, tmpVariableName)
                        .endControlFlow();
            } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getInt($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getBoolean($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getFloat($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getLong($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getDouble($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            }
            if (needsValidation && !paramType.isPrimitive())
                addValidationExpression(codeBlock, parameterName);
        } else if (typeString.equals(dateType) || typeString.equals(OffsetDateType)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.addStatement("$T $LDateString = ".concat(getDateStringExpression).concat(""), STRING, parameterName, parameterName);
            if (needsValidation)
                addValidationExpression(codeBlock, parameterName.concat("DateString"));
            codeBlock.addStatement("$T $L = $T.getDateFromStringInDefaultFormat($L)", typeString.equals(dateType) ? DATE : OFFSET_DATE_TIME, parameterName, typeString.equals(dateType) ? DATE_UTILS : OFFSET_DATE_UTILS, parameterName.concat("DateString"));
            if (needsValidation) addValidationExpression(codeBlock, parameterName);
        } else {
            String bundleExp = "$T $L = ".concat(String.format("%s.getParcelable($S)", bundleVariableName));
            codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            if (needsValidation) addValidationExpression(codeBlock, parameterName);
        }
        codeBlock.addStatement("$L.$L = $L", fragmentVariableName, parameterName, parameterName);
        return codeBlock.build();
    }

    private static CodeBlock generateSaveInstanceStateBlockOfParam(String fragmentVariableName, TypeName type, String parameterKey, String parameterName, String bundleVariableName) {
        String typeString = type.toString();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        if (typeString.equals(stringType)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(uuidType)) {
            codeBlock.addStatement("$T $L = $S", STRING, parameterName, "");
            codeBlock.beginControlFlow("if($L.$L != null)", fragmentVariableName, parameterName)
                    .addStatement("$L = $L.$L.toString()", parameterName, fragmentVariableName, parameterName)
                    .endControlFlow();
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            codeBlock.addStatement(String.format("%s.putInt($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            codeBlock.addStatement(String.format("%s.putBoolean($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            codeBlock.addStatement(String.format("%s.putFloat($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            codeBlock.addStatement(String.format("%s.putLong($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            codeBlock.addStatement(String.format("%s.putDouble($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(dateType)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateStringInDefaultFormat($L.$L);", STRING, parameterName, DATE_UTILS, fragmentVariableName, parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else if (typeString.equals(OffsetDateType)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateStringInDefaultFormat($L.$L);", STRING, parameterName, OFFSET_DATE_UTILS, fragmentVariableName, parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else {
            codeBlock.addStatement(String.format("%s.putParcelable($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        }
        return codeBlock.build();
    }

    private static CodeBlock generateNewInstanceBlockOfParam(TypeName type, String parameterKey, String parameterName, String bundleVariableName) {
        String typeString = type.toString();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        if (typeString.equals(stringType)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(uuidType)) {
            String tmpVariableName = parameterName.concat("String");
            codeBlock.addStatement("$T $L = $S", STRING, tmpVariableName, "");
            codeBlock.beginControlFlow("if($L != null)", parameterName)
                    .addStatement("$L = $L.toString()", tmpVariableName, parameterName)
                    .endControlFlow();
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, tmpVariableName);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            codeBlock.addStatement(String.format("%s.putInt($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            codeBlock.addStatement(String.format("%s.putBoolean($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            codeBlock.addStatement(String.format("%s.putFloat($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            codeBlock.addStatement(String.format("%s.putLong($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            codeBlock.addStatement(String.format("%s.putDouble($S,$L)", bundleVariableName), parameterKey, parameterName);
        } else if (typeString.equals(dateType)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateStringInDefaultFormat($L)", STRING, parameterName, DATE_UTILS, parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else if (typeString.equals(OffsetDateType)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateStringInDefaultFormat($L)", STRING, parameterName, OFFSET_DATE_UTILS, parameterName);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else {
            codeBlock.addStatement(String.format("%s.putParcelable($S,$L)", bundleVariableName), parameterKey, parameterName);
        }
        return codeBlock.build();
    }

    private static void addValidationExpression(CodeBlock.Builder codeBlock, String paramName) {
        codeBlock.beginControlFlow("if(" + paramName + " == null)")
                .addStatement("throw new java.lang.IllegalArgumentException(\"Argument " + paramName + " is not optional.\")")
                .endControlFlow();
    }
}
