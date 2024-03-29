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

import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.FRAGMENT_FACTORY_CLASS_NAME;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("SameParameterValue")
public class FragmentFactoryGenerator extends CodeGenerator {
    private final List<FragmentMetadata> fragmentMetadataList;

    public FragmentFactoryGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        fragmentMetadataList = annotationResultsRegistry.getFragmentDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<TypeSpec> innerClasses = new ArrayList<>();

        TypeSpec.Builder rootClassBuilder = TypeSpec.classBuilder(FRAGMENT_FACTORY_CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

        for (FragmentMetadata fragmentBindingData : fragmentMetadataList) {
            generateFragmentCreator(innerClasses, fragmentBindingData);
        }

        rootClassBuilder.addTypes(innerClasses);

        writeClassFile(packageNames.getFragmentsFactoryPackage(), rootClassBuilder);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasFragmentDescriptions();
    }

    private void generateFragmentCreator(List<TypeSpec> classes, FragmentMetadata fragmentMetadata) {
        List<MethodSpec> methods = new ArrayList<>();

        generateFragmentNewInstanceMethods(fragmentMetadata, methods);

        TypeSpec initializerClass = TypeSpec.classBuilder(fragmentMetadata.getSimpleName())
                .addModifiers(FINAL, PUBLIC)
                .addMethods(methods)
                .build();

        classes.add(initializerClass);

    }

    private void generateFragmentNewInstanceMethods(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        if (!fragmentMetadata.getType().canBeInstantiated()) {
            return;
        }

        if (fragmentMetadata.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentMetadata, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentMetadata, true, destination);
    }

    private void generateFragmentNewInstanceMethod(FragmentMetadata fragmentMetadata, boolean includeOptionalParams, List<MethodSpec> destination) {
        ClassName fragmentClassName = getClassName(fragmentMetadata);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentClassName)
                .addStatement("$T fragment = new $T()", fragmentClassName, fragmentClassName)
                .addStatement("$T arguments = new $T()", ClassNameUtils.ANDROID_BUNDLE, ClassNameUtils.ANDROID_BUNDLE);

        fragmentMetadata.getScreenParameters(includeOptionalParams).forEach(parameter -> {
            Variable variable = new Variable(parameter.getName(),parameter.getType());
            CodeBlock writeIntoBundleCodeBlock = bundleCodeGenerator.generateWriteStatement(variable, "arguments");
            ParameterSpec parameterSpec = generateScreenParameterSpec(parameter);

            builder.addParameter(parameterSpec);
            builder.addCode(writeIntoBundleCodeBlock);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }
}