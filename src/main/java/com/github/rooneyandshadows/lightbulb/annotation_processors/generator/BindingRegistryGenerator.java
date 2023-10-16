package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static javax.lang.model.element.Modifier.*;

public class BindingRegistryGenerator extends CodeGenerator {
    private final String bindingsPackage;
    private final ClassName bindingClassName;
    private final ClassName bindingInitializerClassName;
    private final Class<?> bindingInterfaceClass;

    public BindingRegistryGenerator(String rootPackage, Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(rootPackage, filer, annotationResultsRegistry);
        bindingsPackage = rootPackage.concat(".bindings");
        bindingClassName = ClassName.get(bindingsPackage, "Binding");
        bindingInitializerClassName = ClassName.get(bindingsPackage, "BindingInitializer");
        bindingInterfaceClass = generateClassForName(bindingClassName);
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        if (fragmentBindings.isEmpty()) return;
        generateBindingInterface();
        generateBindingInitializer();
    }

    private void generateBindingInterface() {
        TypeSpec generatedInterface = TypeSpec.interfaceBuilder(bindingClassName)
                .addModifiers(PUBLIC)
                .build();
        try {
            JavaFile.builder(bindingsPackage, generatedInterface)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateBindingInitializer() {
        TypeVariableName typeVariableName = TypeVariableName.get("T", bindingInterfaceClass);
        MethodSpec method = MethodSpec.methodBuilder("initialize")
                .returns(typeVariableName)
                .build();
        TypeSpec generatedInterface = TypeSpec.interfaceBuilder(bindingInitializerClassName)
                .addModifiers(PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", bindingInterfaceClass))
                .addMethod(method)
                .build();
        try {
            JavaFile.builder(bindingsPackage, generatedInterface)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }


    private Class<?> generateClassForName(ClassName className) {
        try {
            return Class.forName(className.canonicalName());
        } catch (Exception e) {
            return null;
        }
    }
}