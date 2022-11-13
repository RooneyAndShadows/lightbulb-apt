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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames.GENERATED_LB_SCREENS;

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
                String methodName = "to" + groupName + screenName;
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
                routerClass.addMethod(methodBuilder.build());
            });
        });
        try {
            JavaFile.builder(PackageNames.GENERATED_LB_ROUTING, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
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
                    "arguments"
            );
            method.addStatement(readStatement);
        });
        return method.build();
    }

    private static MethodSpec generateFragmentNewInstanceMethod(FragmentInfo fragmentInfo, boolean includeOptionalParams) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("newInstance");
        fragmentInfo.getFragmentParameters().forEach(param -> method.addParameter(
                param.getType(),
                param.getName()
        ));
        method.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T  fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T  arguments = new $T()", BUNDLE, BUNDLE);
        fragmentInfo.getFragmentParameters().forEach(param -> {
            boolean acceptParam = !param.isOptional() || (param.isOptional() && includeOptionalParams);
            if (!acceptParam) return;
            CodeBlock writeStatement = resolveWriteParamInBundleExpression(
                    "fragment",
                    param.getType(),
                    param.getName(),
                    param.getName(),
                    "arguments"
            );
            method.addStatement(writeStatement);
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
            CodeBlock writeStatement = resolveWriteParamInBundleExpression(
                    "fragment",
                    param.getType(),
                    param.getName(),
                    param.getName(),
                    "outState"
            );
            method.addStatement(writeStatement);
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
                    "fragmentSavedInstanceState"
            );
            method.addStatement(readStatement);
        });
        return method.build();
    }

    private static boolean hasOptionalParameters(FragmentInfo fragment) {
        for (FragmentParamInfo param : fragment.getFragmentParameters())
            if (param.isOptional()) return true;
        return false;
    }

    @SuppressWarnings("DuplicatedCode")
    private static CodeBlock resolveReadParamFromBundleExpression(String fragmentVariableName, FragmentParamInfo param, String bundleVariableName) {
        String typeString = param.getType().toString();
        String parameterName = param.getName();
        String paramToSetName = parameterName;
        boolean optional = param.isOptional();
        boolean needsValidation = !optional || !param.getType().isPrimitive();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        String expression;
        if (optional || param.getType().isPrimitive()) expression = "$L.$L = ";
        else expression = "$T $L = ";
        if (typeString.equals(stringType)) {
            expression = expression.concat(String.format("%s.getString($S)", bundleVariableName));
        } else if (typeString.equals(uuidType)) {
            expression = expression.concat(String.format("$T.fromString(%s.getString($S))", bundleVariableName));
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            expression = expression.concat(String.format("%s.getInt($S)", bundleVariableName));
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            expression = expression.concat(String.format("%s.getBoolean($S)", bundleVariableName));
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            expression = expression.concat(String.format("%s.getFloat($S)", bundleVariableName));
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            expression = expression.concat(String.format("%s.getLong($S)", bundleVariableName));
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            expression = expression.concat(String.format("%s.getDouble($S)", bundleVariableName));
        } else if (typeString.equals(dateType)) {
            paramToSetName = parameterName.concat("Date");
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.add("$T $LDateString = ".concat(getDateStringExpression).concat(";\n"), STRING, parameterName, parameterName);
            codeBlock.add("$T $LDate = $T.getDateFromStringInDefaultFormat($L);\n", DATE, parameterName, DATE_UTILS, parameterName.concat("DateString"));
            expression = expression.concat("$L");
            codeBlock.add(expression, fragmentVariableName, parameterName, paramToSetName);
        } else if (typeString.equals(OffsetDateType)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            paramToSetName = parameterName.concat("Date");
            codeBlock.add("$T $LDateString = ".concat(getDateStringExpression).concat(";\n"), STRING, parameterName, parameterName);
            codeBlock.add("$T $LDate = $T.getDateFromStringInDefaultFormat($L);\n", OFFSET_DATE_TIME, parameterName, OFFSET_DATE_UTILS, parameterName.concat("DateString"));
            expression = expression.concat("$L");
            codeBlock.add(expression, fragmentVariableName, parameterName, paramToSetName);
        } else {
            expression = expression.concat(String.format("%s.getParcelable($S)", bundleVariableName));
        }
        if (needsValidation) {
            codeBlock.add(";\n");
            codeBlock.beginControlFlow("if(" + paramToSetName + " == null)")
                    .add("throw new java.lang.IllegalArgumentException(\"Argument " + parameterName + " is not optional.\");\n")
                    .endControlFlow();
            codeBlock.add("fragment.$L = $L", parameterName, paramToSetName);
        }else{

        }
        codeBlock.add(expression, fragmentVariableName, parameterName, parameterName);
        //

        return codeBlock.build();
    }

    private static CodeBlock resolveWriteParamInBundleExpression(String fragmentVariableName, TypeName type, String parameterKey, String parameterName, String bundleVariableName) {
        String typeString = type.toString();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        if (typeString.equals(stringType)) {
            codeBlock.add(String.format("%s.putString($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(uuidType)) {
            codeBlock.add(String.format("%s.putString($S,$L.$LtoString())", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            codeBlock.add(String.format("%s.putInt($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            codeBlock.add(String.format("%s.putBoolean($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            codeBlock.add(String.format("%s.putFloat($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            codeBlock.add(String.format("%s.putLong($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            codeBlock.add(String.format("%s.putDouble($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        } else if (typeString.equals(dateType)) {
            codeBlock.add("$T $LDateString = $T.getDateStringInDefaultFormat($L.$L);\n", STRING, parameterName, DATE_UTILS, fragmentVariableName, parameterName);
            codeBlock.add(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else if (typeString.equals(OffsetDateType)) {
            codeBlock.add("$T $LDateString = $T.getDateStringInDefaultFormat($L,$L);\n", STRING, parameterName, OFFSET_DATE_UTILS, fragmentVariableName, parameterName);
            codeBlock.add(String.format("%s.putString($S,$L)", bundleVariableName), parameterKey, parameterName.concat("DateString"));
        } else {
            codeBlock.add(String.format("%s.putParcelable($S,$L.$L)", bundleVariableName), parameterKey, fragmentVariableName, parameterName);
        }
        return codeBlock.build();
    }
}
