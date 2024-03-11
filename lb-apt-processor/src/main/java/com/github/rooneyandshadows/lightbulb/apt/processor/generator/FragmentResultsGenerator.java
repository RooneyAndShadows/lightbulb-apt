package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Variable;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.FRAGMENT_RESULT_CLASS_NAME;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("SameParameterValue")
public class FragmentResultsGenerator extends CodeGenerator {
    private final List<FragmentMetadata> fragmentMetadataList;

    public FragmentResultsGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        fragmentMetadataList = annotationResultsRegistry.getFragmentDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<TypeSpec> innerClasses = new ArrayList<>();

        TypeSpec.Builder rootClassBuilder = TypeSpec.classBuilder(FRAGMENT_RESULT_CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

        fragmentMetadataList.stream().filter(FragmentMetadata::hasResultListeners).toList().forEach(fragmentMetadata -> {
            generateFragmentWrapper(innerClasses, fragmentMetadata);
        });

        rootClassBuilder.addTypes(innerClasses);

        writeClassFile(packageNames.getFragmentsResultPackage(), rootClassBuilder);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.getFragmentDescriptions().stream().anyMatch(FragmentMetadata::hasResultListeners);
    }

    private void generateFragmentWrapper(List<TypeSpec> classes, FragmentMetadata fragmentMetadata) {
        List<MethodSpec> methods = new ArrayList<>();

        generateFragmentResultListeners(fragmentMetadata, methods);

        TypeSpec initializerClass = TypeSpec.classBuilder(fragmentMetadata.getSimpleName())
                .addModifiers(FINAL, PUBLIC)
                .addMethods(methods)
                .build();

        classes.add(initializerClass);

    }

    private void generateFragmentResultListeners(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        fragmentMetadata.getResultListeners().forEach(resultListenerMetadata -> {
            generateFragmentListener(resultListenerMetadata, true, destination);
        });
    }

    private void generateFragmentListener(FragmentMetadata.ResultListenerMetadata resultListenerMetadata, boolean includeOptionalParams, List<MethodSpec> destination) {
        String methodName = resultListenerMetadata.getMethod().getName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(resultListenerMetadata.getMethod().getName())
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class)
                .addParameter(ClassNameUtils.ANDROID_FRAGMENT_MANAGER, "fragManager")
                .addStatement("$T fragmentResultToSend = new $T()", ClassNameUtils.ANDROID_BUNDLE, ClassNameUtils.ANDROID_BUNDLE);

        resultListenerMetadata.getMethod().getParameters(includeOptionalParams).forEach(parameterDefinition -> {
            Variable variable = new Variable(parameterDefinition.getName(), parameterDefinition.getType());
            CodeBlock writeIntoBundleCodeBlock = bundleCodeGenerator.generateWriteStatement(variable, "fragmentResultToSend");
            ParameterSpec parameterSpec = generateParameterSpec(parameterDefinition.getName(), parameterDefinition.getType(), parameterDefinition.isNullable());

            builder.addParameter(parameterSpec);
            builder.addCode(writeIntoBundleCodeBlock);
        });

        builder.addStatement("fragManager.setFragmentResult($S,$L)", methodName, "fragmentResultToSend");

        destination.add(builder.build());
    }
}