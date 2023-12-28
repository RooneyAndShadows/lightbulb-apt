package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("SameParameterValue")
public class FragmentFactoryGenerator extends CodeGenerator {
    private final String fragmentsFactoryPackage;

    public FragmentFactoryGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer,elements, annotationResultsRegistry);
        fragmentsFactoryPackage = PackageNames.getFragmentsFactoryPackage();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbFragmentDescription> fragmentDescriptions = annotationResultsRegistry.getFragmentDescriptions();

        generateFragmentFactory(fragmentDescriptions);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasFragmentDescriptions();
    }

    private void generateFragmentFactory(List<LightbulbFragmentDescription> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(ClassNames.FRAGMENT_FACTORY_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentBindings.forEach(fragmentBindingData -> generateFragmentCreator(rootClass, fragmentBindingData));

        try {
            JavaFile.builder(fragmentsFactoryPackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateFragmentCreator(TypeSpec.Builder fragmentFactoryBuilder, LightbulbFragmentDescription fragment) {
        List<MethodSpec> methods = new ArrayList<>();
        generateFragmentNewInstanceMethods(fragment, methods);

        String initializerClassName = fragment.getClassName().simpleName();

        TypeSpec.Builder builder = TypeSpec.classBuilder(initializerClassName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethods(methods);

        fragmentFactoryBuilder.addType(builder.build());

    }

    private void generateFragmentNewInstanceMethods(LightbulbFragmentDescription fragmentInfo, List<MethodSpec> destination) {
        if (!fragmentInfo.isCanBeInstantiated()) {
            return;
        }

        if (fragmentInfo.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentInfo, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentInfo, true, destination);
    }

    private void generateFragmentNewInstanceMethod(
            LightbulbFragmentDescription fragmentInfo,
            boolean includeOptionalParams,
            List<MethodSpec> destination
    ) {
        ClassName fragmentClassName = fragmentInfo.getClassName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentClassName)
                .addStatement("$T fragment = new $T()", fragmentClassName, fragmentClassName)
                .addStatement("$T arguments = new $T()", ClassNames.ANDROID_BUNDLE, ClassNames.ANDROID_BUNDLE);

        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(parameter -> {
            CodeBlock writeIntoBundleCodeBlock = generateWriteParamIntoBundleBlock(parameter, "arguments");
            ParameterSpec parameterSpec = generateParameterSpec(parameter);

            builder.addParameter(parameterSpec);
            builder.addCode(writeIntoBundleCodeBlock);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }

    private CodeBlock generateWriteParamIntoBundleBlock(FieldScreenParameter parameter, String bundleVariableName) {
        TypeInformation parameterTypeInfo = parameter.getTypeInformation();
        String variableName = parameter.getName();
        CodeBlock.Builder writeIntoBundleCodeBlock = CodeBlock.builder();

        BundleCodeGenerator.generateWriteStatement(writeIntoBundleCodeBlock, parameterTypeInfo, bundleVariableName, variableName, variableName);

        return writeIntoBundleCodeBlock.build();
    }
}