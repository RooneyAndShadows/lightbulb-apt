package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.PackageNames.ANDROID_VIEW;
import static javax.lang.model.element.Modifier.*;

public class FragmentGenerator extends CodeGenerator {

    public FragmentGenerator(String rootPackage, Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(rootPackage, filer, annotationResultsRegistry);
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        generateBindings(fragmentBindings);
    }

    private void generateBindings(List<FragmentBindingData> fragmentBindings) {
        fragmentBindings.forEach(fragmentInfo -> {
            List<MethodSpec> methods = new ArrayList<>();
            if (fragmentInfo.isCanBeInstantiated()) {
                if (fragmentInfo.hasOptionalParameters()) {
                    methods.add(generateFragmentNewInstanceMethod(fragmentInfo, false));
                }
                methods.add(generateFragmentNewInstanceMethod(fragmentInfo, true));
            }
            methods.add(generateFragmentConfigurationMethod(fragmentInfo));
            methods.add(generateFragmentViewBindingsMethod(fragmentInfo));
            methods.add(generateFragmentParametersMethod(fragmentInfo));
            methods.add(generateSaveVariablesMethod(fragmentInfo));
            methods.add(generateRestoreVariablesMethod(fragmentInfo));
            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(fragmentInfo.getBindingClassName())
                    .addModifiers(PUBLIC, FINAL)
                    .addMethods(methods);
            try {
                JavaFile.builder(fragmentInfo.getBindingClassName().packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private MethodSpec generateFragmentNewInstanceMethod(FragmentBindingData fragmentInfo, boolean includeOptionalParams) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("newInstance");
        method.addModifiers(PUBLIC, STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T arguments = new $T()", ANDROID_BUNDLE, ANDROID_BUNDLE);
        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(param -> {
            method.addParameter(param.getParameterSpec());
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "arguments", null);
            method.addCode(writeStatement);
        });
        method.addStatement("fragment.setArguments(arguments)");
        method.addStatement("return fragment");
        return method.build();
    }

    private MethodSpec generateFragmentConfigurationMethod(FragmentBindingData fragment) {
        String methodName = "generateConfiguration";
        Configuration configuration = fragment.getConfiguration();
        if (configuration == null) {
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC)
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
                .addModifiers(PUBLIC)
                .addParameter(fragment.getClassName(), "fragment")
                .returns(BASE_FRAGMENT_CONFIGURATION)
                .addStatement("int layoutId = fragment.getResources().getIdentifier($S, $S, fragment.getActivity().getPackageName())", layoutName, "layout")
                .addStatement("return new $T($L,$L,$L,$L)", BASE_FRAGMENT_CONFIGURATION, "layoutId", isMainScreenFragment, hasLeftDrawer, hasOptionsMenu)
                .build();
    }

    private MethodSpec generateFragmentViewBindingsMethod(FragmentBindingData fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateViewBindings")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(PUBLIC)
                .returns(void.class);
        fragment.getViewBindings().forEach(bindingInfo -> {
            String fieldName = bindingInfo.getFieldName();
            String fieldSetterName = bindingInfo.getSetterName();
            String resourceName = bindingInfo.getResourceName();
            method.addStatement("$T packageName = fragment.getActivity().getPackageName()", STRING);
            method.addStatement("$T resources = fragment.getResources()", ANDROID_RESOURCES);
            method.addStatement("$T view = fragment.getView()", ANDROID_VIEW);
            if (bindingInfo.hasSetter()) {
                String statement = "fragment.$L(view.findViewById(resources.getIdentifier($S, $S, packageName)))";
                method.addStatement(statement, fieldSetterName, resourceName, "id");
            } else {
                String statement = "fragment.$L = view.findViewById(resources.getIdentifier($S, $S, packageName))";
                method.addStatement(statement, fieldName, resourceName, "id");
            }
        });
        return method.build();
    }

    private MethodSpec generateFragmentParametersMethod(FragmentBindingData fragment) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("generateParameters")
                .addParameter(fragment.getClassName(), "fragment")
                .addModifiers(PUBLIC)
                .returns(void.class);
        method.addStatement("$T arguments = fragment.getArguments()", ANDROID_BUNDLE);
        fragment.getParameters().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "arguments", "fragment", true);
            method.addCode(readStatement);
        });
        return method.build();
    }

    private MethodSpec generateSaveVariablesMethod(FragmentBindingData fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(PUBLIC)
                .addParameter(ANDROID_BUNDLE, "outState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "outState", "fragment");
            method.addCode(writeStatement);
        });
        fragmentInfo.getPersistedVariables().forEach(param -> {
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "outState", "fragment");
            method.addCode(writeStatement);
        });
        return method.build();
    }

    private MethodSpec generateRestoreVariablesMethod(FragmentBindingData fragmentInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(PUBLIC)
                .addParameter(ANDROID_BUNDLE, "fragmentSavedInstanceState")
                .addParameter(fragmentInfo.getClassName(), "fragment")
                .returns(void.class);
        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "fragmentSavedInstanceState", "fragment", false);
            method.addCode(readStatement);
        });
        fragmentInfo.getPersistedVariables().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "fragmentSavedInstanceState", "fragment", false);
            method.addCode(readStatement);
        });
        return method.build();
    }
}