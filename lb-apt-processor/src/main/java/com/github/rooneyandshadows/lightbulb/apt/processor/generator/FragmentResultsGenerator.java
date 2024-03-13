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
        List<MethodSpec> methods = new ArrayList<>();

        TypeSpec.Builder rootClassBuilder = TypeSpec.classBuilder(FRAGMENT_RESULT_CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

        fragmentMetadataList.stream().filter(FragmentMetadata::hasResultListeners).toList().forEach(fragmentMetadata -> {
            String innerClassNameString = fragmentMetadata.getSimpleName();
            List<MethodSpec> innerClassMethods = new ArrayList<>();

            fragmentMetadata.getResultListeners().forEach(resultListenerMetadata -> {
                generateFragmentListener(fragmentMetadata, resultListenerMetadata, innerClassMethods);
            });

            TypeSpec resultClass = TypeSpec.classBuilder(innerClassNameString)
                    .addModifiers(FINAL, PUBLIC, STATIC)
                    .addMethods(innerClassMethods)
                    .build();

            String methodName = "get".concat(resultClass.name);
            ClassName innerClassName = ClassName.get("", resultClass.name);

            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC, FINAL)
                    .returns(innerClassName)
                    .addStatement("return new $T()", innerClassName);

            methods.add(builder.build());
            innerClasses.add(resultClass);
        });

        rootClassBuilder.addMethods(methods);
        rootClassBuilder.addTypes(innerClasses);

        writeClassFile(packageNames.getFragmentsResultPackage(), rootClassBuilder);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.getFragmentDescriptions().stream().anyMatch(FragmentMetadata::hasResultListeners);
    }

    private void generateFragmentListener(FragmentMetadata fragmentHost, FragmentMetadata.ResultListenerMetadata resultListenerMetadata, List<MethodSpec> destination) {
        String methodName = resultListenerMetadata.getMethod().getName();
        String methodEnclosingClassSimpleName = fragmentHost.getType().getQualifiedName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(resultListenerMetadata.getMethod().getName())
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class)
                .addParameter(ClassNameUtils.ANDROID_FRAGMENT_MANAGER, "fragManager")
                .addStatement("$T fragmentResultToSend = new $T()", ClassNameUtils.ANDROID_BUNDLE, ClassNameUtils.ANDROID_BUNDLE);

        resultListenerMetadata.getMethod().getParameters(true).forEach(parameterDefinition -> {
            Variable variable = new Variable(parameterDefinition.getName(), parameterDefinition.getType());
            CodeBlock writeIntoBundleCodeBlock = bundleCodeGenerator.generateWriteStatement(variable, "fragmentResultToSend");
            ParameterSpec parameterSpec = generateParameterSpec(parameterDefinition.getName(), parameterDefinition.getType(), parameterDefinition.isNullable());

            builder.addParameter(parameterSpec);
            builder.addCode(writeIntoBundleCodeBlock);
        });

        String tag = String.format("%s_%s", methodEnclosingClassSimpleName, methodName);

        builder.addStatement("fragManager.setFragmentResult($S,$L)", tag, "fragmentResultToSend");

        destination.add(builder.build());
    }
}