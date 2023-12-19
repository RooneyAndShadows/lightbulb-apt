package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.MemberUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.element.Modifier.PUBLIC;

@SuppressWarnings("DuplicatedCode")
public class ServiceGenerator extends CodeGenerator {
    private final String servicePackage;
    private final boolean hasRoutingElements;
    private final boolean hasStorageElements;

    public ServiceGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        servicePackage = PackageNames.getServicePackage();
        hasRoutingElements = annotationResultsRegistry.hasRoutingScreens();
        hasStorageElements = annotationResultsRegistry.hasStorageDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbStorageDescription> storageDescriptions = annotationResultsRegistry.getStorageDescriptions();

        generateServiceSingleton(storageDescriptions);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return hasStorageElements || hasRoutingElements;
    }

    private void generateServiceSingleton(List<LightbulbStorageDescription> storageDescriptions) {
        ClassName lightbulbServiceClassName = ClassNames.getLightbulbServiceClassName();
        List<FieldSpec> fields = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();

        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(lightbulbServiceClassName)
                .addModifiers(PUBLIC, FINAL)
                .addField(lightbulbServiceClassName, "instance", PRIVATE, STATIC)
                .addMethod(MethodSpec.methodBuilder("getInstance")
                        .addModifiers(PUBLIC, STATIC, SYNCHRONIZED)
                        .returns(lightbulbServiceClassName)
                        .beginControlFlow("if(instance == null)")
                        .addStatement("instance = new $T()", lightbulbServiceClassName)
                        .endControlFlow()
                        .addStatement("return instance")
                        .build()
                );

        generateRoutingMember(fields, methods);
        generateStorageMembers(storageDescriptions, fields, methods);
        generateBindRouterMethod(methods);
        generateUnbindRouterMethod(methods);
        generateBindStoragesMethod(storageDescriptions, methods);

        singletonClass.addFields(fields);
        singletonClass.addMethods(methods);

        try {
            JavaFile.builder(servicePackage, singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateStorageMembers(List<LightbulbStorageDescription> storageDescriptions, List<FieldSpec> fields, List<MethodSpec> methods) {
        storageDescriptions.forEach(storageDescription -> {
            ClassName generatedStorageClassName = storageDescription.getInstrumentedClassName();
            String generatedStorageSimpleClassName = generatedStorageClassName.simpleName();
            String fieldName = MemberUtils.getFieldNameForClass(generatedStorageSimpleClassName);
            String getterName = MemberUtils.getFieldGetterName(generatedStorageSimpleClassName);

            FieldSpec storageField = FieldSpec.builder(generatedStorageClassName, fieldName, PRIVATE)
                    .build();

            MethodSpec storageGetter = MethodSpec.methodBuilder(getterName)
                    .addModifiers(PUBLIC, FINAL)
                    .returns(generatedStorageClassName)
                    .addStatement("return $L", fieldName)
                    .build();

            fields.add(storageField);
            methods.add(storageGetter);
        });
    }

    private void generateRoutingMember(List<FieldSpec> fields, List<MethodSpec> methods) {
        ClassName routerClassName = ClassNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String fieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);
        String getterName = MemberUtils.getFieldGetterName(generatedRouterSimpleClassName);

        FieldSpec routerField = FieldSpec.builder(routerClassName, fieldName, PRIVATE)
                .build();

        MethodSpec routerGetter = MethodSpec.methodBuilder(getterName)
                .addModifiers(PUBLIC, FINAL)
                .returns(routerClassName)
                .addStatement("return $L", fieldName)
                .build();

        MethodSpec routeMethod = MethodSpec.methodBuilder("route")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .returns(routerClassName)
                .addStatement("return getInstance().$L()", getterName)
                .build();

        fields.add(routerField);
        methods.add(routerGetter);
        methods.add(routeMethod);
    }

    private void generateBindRouterMethod(List<MethodSpec> methods) {
        if (!hasRoutingElements) return;

        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bindRouter")
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class);

        ClassName routerClassName = ClassNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String routerFieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);

        bindMethodBuilder.addParameter(routerClassName, routerFieldName);
        bindMethodBuilder.addStatement("this.$L = $L", routerFieldName, routerFieldName);
        bindMethodBuilder.addStatement("this.$L.attach()", routerFieldName);

        methods.add(bindMethodBuilder.build());
    }

    private void generateUnbindRouterMethod(List<MethodSpec> methods) {
        if (!hasRoutingElements) return;

        MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbindRouter")
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class);

        ClassName routerClassName = ClassNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String routerFieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);

        unbindMethodBuilder.addStatement("this.$L.detach()", routerFieldName);
        unbindMethodBuilder.addStatement("this.$L = null", routerFieldName);

        methods.add(unbindMethodBuilder.build());
    }

    private void generateBindStoragesMethod(List<LightbulbStorageDescription> storageDescriptions, List<MethodSpec> methods) {
        if (!hasStorageElements) return;

        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bindStorages")
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class);

        storageDescriptions.forEach(storageDescription -> {
            ClassName generatedStorageClassName = storageDescription.getInstrumentedClassName();
            String generatedStorageSimpleClassName = generatedStorageClassName.simpleName();
            String fieldName = MemberUtils.getFieldNameForClass(generatedStorageSimpleClassName);

            bindMethodBuilder.addParameter(generatedStorageClassName, fieldName);
            bindMethodBuilder.addStatement("this.$L = $L", fieldName, fieldName);
        });

        methods.add(bindMethodBuilder.build());
    }
}
