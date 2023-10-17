package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

//TODO MAKE FRAGMENT BINDINGS EXTEND BINDING
//TODO ADD BINDINGS INTO BINDING REGISTRY CONSTRUCTOR

public class BindingRegistryGenerator extends CodeGenerator {
    private final String bindingsPackage;
    private final ClassName bindingClassName;
    private final ClassName bindingInitializerClassName;
    private final ClassName bindingWrapperClassName;
    private final ClassName bindingsRegistryClassName;

    public BindingRegistryGenerator(String rootPackage, Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(rootPackage, filer, annotationResultsRegistry);
        bindingsPackage = rootPackage.concat(".bindings");
        bindingClassName = ClassName.get(bindingsPackage, "Binding");
        bindingInitializerClassName = ClassName.get(bindingsPackage, "BindingInitializer");
        bindingWrapperClassName = ClassName.get(bindingsPackage, "BindingWrapper");
        bindingsRegistryClassName = ClassName.get(bindingsPackage, "BindingsRegistry");
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        if (fragmentBindings.isEmpty()) return;
        generateBindingInterface();
        generateBindingInitializer();
        generateBindingWrapper();
        generateBindingsRegistrySingleton(fragmentBindings);
    }

    private void generateBindingInterface() {
        TypeSpec bindingInterface = TypeSpec.interfaceBuilder(bindingClassName)
                .addModifiers(PUBLIC)
                .build();
        try {
            JavaFile.builder(bindingsPackage, bindingInterface)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateBindingInitializer() {
        TypeVariableName typeVariableName = TypeVariableName.get("T", bindingClassName);

        MethodSpec method = MethodSpec.methodBuilder("initialize")
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(typeVariableName)
                .build();

        TypeSpec bindingInitializerInterface = TypeSpec.interfaceBuilder(bindingInitializerClassName)
                .addModifiers(PUBLIC)
                .addAnnotation(FunctionalInterface.class)
                .addTypeVariable(typeVariableName)
                .addMethod(method)
                .build();
        try {
            JavaFile.builder(bindingsPackage, bindingInitializerInterface)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateBindingWrapper() {
        TypeVariableName bindingTypeVariableName = TypeVariableName.get("T", bindingClassName);
        ParameterizedTypeName initializerTypeName = ParameterizedTypeName.get(bindingInitializerClassName, bindingTypeVariableName);

        FieldSpec initializerField = FieldSpec.builder(initializerTypeName, "initializer", PRIVATE, FINAL)
                .build();
        FieldSpec bindingField = FieldSpec.builder(bindingTypeVariableName, "binding", PRIVATE)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(initializerTypeName, "initializer")
                .addStatement("this.initializer = initializer")
                .build();

        MethodSpec getBindingMethod = MethodSpec.methodBuilder("getBinding")
                .addModifiers(PUBLIC)
                .returns(bindingTypeVariableName)
                .beginControlFlow("if(binding == null)")
                .addStatement("binding = initializer.initialize()")
                .endControlFlow()
                .addStatement("return binding")
                .build();

        TypeSpec bindingWrapperClass = TypeSpec.classBuilder(bindingWrapperClassName)
                .addModifiers(PUBLIC)
                .addTypeVariable(bindingTypeVariableName)
                .addMethod(constructor)
                .addMethod(getBindingMethod)
                .addField(initializerField)
                .addField(bindingField)
                .build();

        try {
            JavaFile.builder(bindingsPackage, bindingWrapperClass)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    //TODO GENERALIZE INPUT PARAM TO BE ABSTRAT BINDING TYPE
    private void generateBindingsRegistrySingleton(List<FragmentBindingData> fragmentBindings) {
        TypeName objectWildcard = WildcardTypeName.subtypeOf(OBJECT);
        TypeName bindingWildcard = WildcardTypeName.subtypeOf(bindingClassName);
        ParameterizedTypeName clsWildcard = ParameterizedTypeName.get(CLASS, objectWildcard);
        ParameterizedTypeName valTypeName = ParameterizedTypeName.get(bindingWrapperClassName, bindingWildcard);
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(MAP, clsWildcard, valTypeName);

        FieldSpec initializerField = FieldSpec.builder(mapTypeName, "bindings", PRIVATE, FINAL)
                .initializer("new $T<>()", HASH_MAP)
                .build();

        FieldSpec singleInstanceField = FieldSpec.builder(bindingsRegistryClassName, "single_instance", PRIVATE, STATIC, FINAL)
                .initializer("new $T()", bindingsRegistryClassName)
                .build();

        MethodSpec getInstanceMethod = MethodSpec.methodBuilder("getInstance")
                .addModifiers(PUBLIC, STATIC, SYNCHRONIZED)
                .returns(bindingsRegistryClassName)
                .addStatement("return single_instance")
                .build();

        TypeVariableName bindingTypeVariableName = TypeVariableName.get("T", bindingClassName);
        ParameterizedTypeName bindingWrapperTypeName = ParameterizedTypeName.get(bindingWrapperClassName, bindingTypeVariableName);

        MethodSpec getBindingForClassMethod = MethodSpec.methodBuilder("getInstance")
                .addModifiers(PUBLIC, STATIC, SYNCHRONIZED)
                .addParameter(clsWildcard, "target")
                .addTypeVariable(bindingTypeVariableName)
                .returns(bindingTypeVariableName)
                .addStatement("final $T bindingWrapper = (BindingWrapper<$T>) single_instance.bindings.get(target)", bindingWrapperTypeName, bindingTypeVariableName)
                .beginControlFlow("if(bindingWrapper == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return bindingWrapper.getBinding()")
                .build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE);

        fragmentBindings.forEach(fragmentBindingData -> {
            ClassName classSimpleName = fragmentBindingData.getClassName();
            ClassName bindingClassSimpleName = fragmentBindingData.getBindingClassName();
            constructorBuilder.addStatement("single_instance.bindings.put($T.class,new $T<>($T::new))", classSimpleName, bindingWrapperClassName, bindingClassSimpleName);
        });

        MethodSpec constructor = constructorBuilder.build();

        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(bindingsRegistryClassName)
                .addModifiers(PUBLIC)
                .addField(singleInstanceField)
                .addField(initializerField)
                .addMethod(constructor)
                .addMethod(getInstanceMethod)
                .addMethod(getBindingForClassMethod);
        try {
            JavaFile.builder(bindingsPackage, singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}