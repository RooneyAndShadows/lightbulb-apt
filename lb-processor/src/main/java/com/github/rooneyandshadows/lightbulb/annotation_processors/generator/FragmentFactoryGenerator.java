package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

public class FragmentFactoryGenerator extends CodeGenerator {
    private final String fragmentsFactoryPackage;

    public FragmentFactoryGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        fragmentsFactoryPackage = PackageNames.getFragmentsFactoryPackage();
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        generateFragmentFactory(fragmentBindings);
    }

    private void generateFragmentFactory(List<FragmentBindingData> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(FRAGMENT_FACTORY_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentBindings.forEach(fragmentBindingData -> {
            generateFragmentCreator(rootClass, fragmentBindingData);
        });

        try {
            JavaFile.builder(fragmentsFactoryPackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateFragmentCreator(TypeSpec.Builder fragmentFactoryBuilder, FragmentBindingData fragment) {
        List<MethodSpec> methods = new ArrayList<>();
        generateFragmentNewInstanceMethods(fragment, methods);

        String initializerClassName = fragment.getClassName().simpleName();
        TypeSpec.Builder builder = TypeSpec.classBuilder(initializerClassName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethods(methods);

        fragmentFactoryBuilder.addType(builder.build());

    }

    private void generateFragmentNewInstanceMethods(FragmentBindingData fragmentInfo, List<MethodSpec> destination) {
        if (!fragmentInfo.isCanBeInstantiated()) {
            return;
        }

        if (fragmentInfo.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentInfo, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentInfo, true, destination);
    }

    private void generateFragmentNewInstanceMethod(
            FragmentBindingData fragmentInfo,
            boolean includeOptionalParams,
            List<MethodSpec> destination
    ) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T arguments = new $T()", ANDROID_BUNDLE, ANDROID_BUNDLE);

        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(param -> {
            builder.addParameter(param.getParameterSpec());
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "arguments", null);
            builder.addCode(writeStatement);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }
}