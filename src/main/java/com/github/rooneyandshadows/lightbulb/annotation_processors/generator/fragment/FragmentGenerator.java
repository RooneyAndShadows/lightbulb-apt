package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.Variable;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.TypeUtils.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.TypeUtils.isOffsetDate;

public class FragmentGenerator extends CodeGenerator {

    public FragmentGenerator(String rootPackage, Filer filer) {
        super(rootPackage, filer);
    }

    public void generateFragmentBindingClasses(List<FragmentBindingData> fragmentInfoList) {
        List<MethodSpec> methods = new ArrayList<>();
        fragmentInfoList.forEach(fragmentInfo -> {
            methods.clear();
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
                    .classBuilder(fragmentInfo.getBindingClassName())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methods);
            try {
                JavaFile.builder(fragmentInfo.getBindingClassName().packageName(), generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private MethodSpec generateFragmentConfigurationMethod(FragmentBindingData fragment) {
        String methodName = "generateConfiguration";
        Configuration configuration = fragment.getConfiguration();
        if (configuration == null) {
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(fragment.getClassName(), "fragment")
                    .returns(BASE_FRAGMENT_CONFIGURATION)
                    .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, -1, true, false, false)
                    .build();
        }
        String layoutName = configuration.getLayoutName();
        String isMainScreenFragment = String.valueOf(configuration.isMainScreenFragment());
        String hasLeftDrawer = String.valueOf(configuration.isHasLeftDrawer());
        String hasOptionsMenu = String.valueOf(configuration.isHasOptionsMenu());
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(fragment.getClassName(), "fragment")
                .returns(BASE_FRAGMENT_CONFIGURATION)
                .addStatement("int layoutId = fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName())", layoutName, "layout")
                .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, "layoutId", isMainScreenFragment, hasLeftDrawer, hasOptionsMenu)
                .build();
    }

    private MethodSpec generateFragmentViewBindingsMethod(FragmentBindingData fragment) {
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

    private MethodSpec generateFragmentParametersMethod(FragmentBindingData fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateParameters")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        method.addStatement("$T arguments = fragment.getArguments()", BUNDLE);
        fragment.getParameters().forEach(param -> {
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

    private MethodSpec generateFragmentNewInstanceMethod(FragmentBindingData fragmentInfo, boolean includeOptionalParams) {
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

    private MethodSpec generateSaveVariablesMethod(FragmentBindingData fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BUNDLE, "outState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock writeStatement = generateSaveInstanceStateBlockOfParam(
                    param,
                    "fragment",
                    "outState"
            );
            method.addCode(writeStatement);
        });
        fragmentInfo.getPersistedVariables().forEach(param -> {
            CodeBlock writeStatement = generateSaveInstanceStateBlockOfParam(
                    param,
                    "fragment",
                    "outState"
            );
            method.addCode(writeStatement);
        });
        return method.build();
    }

    private MethodSpec generateRestoreVariablesMethod(FragmentBindingData fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(BUNDLE, "fragmentSavedInstanceState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock readStatement = resolveReadParamFromBundleExpression(
                    "fragment",
                    param,
                    "fragmentSavedInstanceState",
                    false
            );
            method.addCode(readStatement);
        });
        fragmentInfo.getPersistedVariables().forEach(param -> {
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

    private CodeBlock resolveReadParamFromBundleExpression(String fragmentVariableName, Variable parameter, String bundleVariableName, boolean validateParameters) {
        String typeString = parameter.getType().toString();
        TypeName paramType = parameter.getType();
        String parameterName = parameter.getName();
        boolean isNullable = parameter.isNullable() || ((parameter instanceof Parameter) && ((Parameter) parameter).isOptional());
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

    private CodeBlock generateSaveInstanceStateBlockOfParam(Variable parameter, String fragmentVariableName, String bundleVariableName) {
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

    private CodeBlock generateNewInstanceBlockOfParam(Parameter parameter, String bundleVariableName) {
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
