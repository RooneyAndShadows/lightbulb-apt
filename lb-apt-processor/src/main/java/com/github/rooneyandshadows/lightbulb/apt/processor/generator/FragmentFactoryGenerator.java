package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.LightbulbFragmentData;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

public class FragmentFactoryGenerator extends CodeGenerator {
    private final String fragmentsFactoryPackage;

    public FragmentFactoryGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        fragmentsFactoryPackage = PackageNames.getFragmentsFactoryPackage();
    }

    @Override
    public void generate() {
        List<LightbulbFragmentData> fragmentBindings = annotationResultsRegistry.getResult(AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS);
        generateFragmentFactory(fragmentBindings);
    }

    private void generateFragmentFactory(List<LightbulbFragmentData> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(ClassNames.FRAGMENT_FACTORY_CLASS_NAME)
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

    private void generateFragmentCreator(TypeSpec.Builder fragmentFactoryBuilder, LightbulbFragmentData fragment) {
        List<MethodSpec> methods = new ArrayList<>();
        generateFragmentNewInstanceMethods(fragment, methods);

        String initializerClassName = fragment.className().simpleName();

        TypeSpec.Builder builder = TypeSpec.classBuilder(initializerClassName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethods(methods);

        fragmentFactoryBuilder.addType(builder.build());

    }

    private void generateFragmentNewInstanceMethods(LightbulbFragmentData fragmentInfo, List<MethodSpec> destination) {
        if (!fragmentInfo.canBeInstantiated()) {
            return;
        }

        if (fragmentInfo.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentInfo, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentInfo, true, destination);
    }

    private void generateFragmentNewInstanceMethod(
            LightbulbFragmentData fragmentInfo,
            boolean includeOptionalParams,
            List<MethodSpec> destination
    ) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentInfo.className())
                .addStatement("$T fragment = new $T()", fragmentInfo.className(), fragmentInfo.className())
                .addStatement("$T arguments = new $T()", ClassNames.ANDROID_BUNDLE, ClassNames.ANDROID_BUNDLE);

        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(parameter -> {
            builder.addParameter(generateParameterSpec(parameter));
            CodeBlock writeStatement = generateWriteIntoBundleBlock(parameter, "arguments", null, false);
            builder.addCode(writeStatement);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }
}