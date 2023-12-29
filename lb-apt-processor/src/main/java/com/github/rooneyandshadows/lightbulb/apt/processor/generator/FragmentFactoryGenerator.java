package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.BundleCodeGenerator;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getFragmentsFactoryPackage;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("SameParameterValue")
public class FragmentFactoryGenerator extends CodeGenerator {
    private final List<FragmentMetadata> fragmentMetadataList;

    public FragmentFactoryGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, annotationResultsRegistry);
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

        writeClassFile(getFragmentsFactoryPackage(), rootClassBuilder);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasFragmentDescriptions();
    }

    private void generateFragmentCreator(List<TypeSpec> classes, FragmentMetadata fragmentMetadata) {
        ClassName fragmentClassName = fragmentMetadata.getClassName();
        String initializerClassName = fragmentClassName.simpleName();
        List<MethodSpec> methods = new ArrayList<>();

        generateFragmentNewInstanceMethods(fragmentMetadata, methods);

        TypeSpec initializerClass = TypeSpec.classBuilder(initializerClassName)
                .addModifiers(FINAL, PUBLIC)
                .addMethods(methods)
                .build();

        classes.add(initializerClass);

    }

    private void generateFragmentNewInstanceMethods(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        TypeInformation fragmentType = new TypeInformation(fragmentMetadata.getElement().asType());

        if (!fragmentType.canBeInstantiated()) {
            return;
        }

        if (fragmentMetadata.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentMetadata, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentMetadata, true, destination);
    }

    private void generateFragmentNewInstanceMethod(FragmentMetadata fragmentMetadata, boolean includeOptionalParams, List<MethodSpec> destination) {
        ClassName fragmentClassName = fragmentMetadata.getClassName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentClassName)
                .addStatement("$T fragment = new $T()", fragmentClassName, fragmentClassName)
                .addStatement("$T arguments = new $T()", ANDROID_BUNDLE, ANDROID_BUNDLE);

        fragmentMetadata.getScreenParameters(includeOptionalParams).forEach(parameter -> {
            ParameterSpec parameterSpec = generateParameterSpec(parameter);
            CodeBlock writeIntoBundleCodeBlock = generateWriteParamIntoBundleBlock(parameter, "arguments");

            builder.addParameter(parameterSpec);
            builder.addCode(writeIntoBundleCodeBlock);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }

    private CodeBlock generateWriteParamIntoBundleBlock(Parameter parameter, String bundleVariableName) {
        TypeInformation parameterTypeInfo = parameter.getTypeInformation();
        String variableName = parameter.getName();
        CodeBlock.Builder writeIntoBundleCodeBlock = CodeBlock.builder();

        BundleCodeGenerator.generateWriteStatement(writeIntoBundleCodeBlock, parameterTypeInfo, bundleVariableName, variableName, variableName);

        return writeIntoBundleCodeBlock.build();
    }
}