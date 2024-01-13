package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.StorageMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.MemberUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.ANDROID_APPLICATION;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.ANDROID_CONTEXT;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.element.Modifier.PUBLIC;

@SuppressWarnings("DuplicatedCode")
public class ServiceGenerator extends CodeGenerator {
    private final String servicePackage;
    private final boolean hasRoutingElements;
    private final boolean hasStorageElements;
    private final List<StorageMetadata> storageMetadataList;

    public ServiceGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        servicePackage = packageNames.getServicePackage();
        hasRoutingElements = annotationResultsRegistry.hasRoutingScreens();
        hasStorageElements = annotationResultsRegistry.hasStorageDescriptions();
        storageMetadataList = annotationResultsRegistry.getStorageDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        generateServiceSingleton();
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return hasStorageElements || hasRoutingElements;
    }

    private void generateServiceSingleton() {
        ClassName lightbulbServiceClassName = classNames.getLightbulbServiceClassName();
        List<FieldSpec> fields = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();

        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(lightbulbServiceClassName)
                .addModifiers(PUBLIC, FINAL);

        generateInstance(lightbulbServiceClassName, fields, methods);
        generateContextMember(fields, methods);
        generateRoutingMember(fields, methods);
        generateStorageMembers(fields, methods);
        generateBindRouterMethod(methods);
        generateUnbindRouterMethod(methods);

        singletonClass.addFields(fields);
        singletonClass.addMethods(methods);

        try {
            JavaFile.builder(servicePackage, singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateInstance(ClassName lightbulbServiceClassName, List<FieldSpec> fields, List<MethodSpec> methods) {
        FieldSpec instanceField = FieldSpec.builder(lightbulbServiceClassName, "instance", PRIVATE, STATIC)
                .build();

        MethodSpec getInstanceMethod = MethodSpec.methodBuilder("getInstance")
                .addModifiers(PUBLIC, STATIC, SYNCHRONIZED)
                .returns(lightbulbServiceClassName)
                .beginControlFlow("if(instance == null)")
                .addStatement("instance = new $T()", lightbulbServiceClassName)
                .endControlFlow()
                .addStatement("return instance")
                .build();

        fields.add(instanceField);
        methods.add(getInstanceMethod);
    }


    private void generateContextMember(List<FieldSpec> fields, List<MethodSpec> methods) {
        String contextFieldName = "applicationContext";
        String getterName = MemberUtils.getFieldGetterName(contextFieldName);
        String setterName = MemberUtils.getFieldSetterName(contextFieldName);

        FieldSpec field = FieldSpec.builder(ANDROID_CONTEXT, contextFieldName, PRIVATE)
                .build();

        ParameterSpec applicationParameter = ParameterSpec.builder(ANDROID_APPLICATION, "application")
                .addAnnotation(NotNull.class)
                .build();

        String applicationContextVarName = "applicationContext";

        MethodSpec.Builder setterMethodBuilder = MethodSpec.methodBuilder(setterName)
                .addModifiers(PUBLIC, FINAL)
                .addParameter(applicationParameter)
                .returns(void.class)
                .addStatement("$T $L = $L.$L", ANDROID_CONTEXT, applicationContextVarName, applicationParameter.name, "getApplicationContext()")
                .addStatement("this.$L = $L", contextFieldName, applicationContextVarName);

        if (hasStorageElements) {
            storageMetadataList.forEach(storageMetadata -> {
                ClassName storageClassName = getInstrumentedClassName(packageNames.getStoragePackage(), storageMetadata, false);
                String storageFieldName = MemberUtils.getFieldNameForClass(storageClassName.simpleName());

                setterMethodBuilder.addStatement("this.$L = new $T($L)", storageFieldName, storageClassName, applicationContextVarName);
            });
        }

        MethodSpec setterMethod = setterMethodBuilder.build();

        MethodSpec getterMethod = MethodSpec.methodBuilder(getterName)
                .addModifiers(PUBLIC, STATIC, FINAL)
                .returns(ANDROID_CONTEXT)
                .addAnnotation(NotNull.class)
                .addStatement("return getInstance().$L", contextFieldName)
                .build();

        fields.add(field);
        methods.add(setterMethod);
        methods.add(getterMethod);
    }

    private void generateStorageMembers(List<FieldSpec> fields, List<MethodSpec> methods) {
        storageMetadataList.forEach(storageDescription -> {
            ClassName generatedStorageClassName = getInstrumentedClassName(packageNames.getStoragePackage(), storageDescription, false);
            String generatedStorageSimpleClassName = generatedStorageClassName.simpleName();
            String fieldName = MemberUtils.getFieldNameForClass(generatedStorageSimpleClassName);
            String getterName = MemberUtils.getFieldGetterName(generatedStorageSimpleClassName);

            FieldSpec storageField = FieldSpec.builder(generatedStorageClassName, fieldName, PRIVATE)
                    .build();

            MethodSpec storageGetter = MethodSpec.methodBuilder(getterName)
                    .addModifiers(PUBLIC, STATIC, FINAL)
                    .addAnnotation(NotNull.class)
                    .returns(generatedStorageClassName)
                    .addStatement("return getInstance().$L", fieldName)
                    .build();

            fields.add(storageField);
            methods.add(storageGetter);
        });
    }

    private void generateRoutingMember(List<FieldSpec> fields, List<MethodSpec> methods) {
        ClassName routerClassName = classNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String fieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);

        FieldSpec routerField = FieldSpec.builder(routerClassName, fieldName, PRIVATE)
                .build();

        MethodSpec routeMethod = MethodSpec.methodBuilder("route")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addAnnotation(NotNull.class)
                .returns(routerClassName)
                .addStatement("return getInstance().$L", fieldName)
                .build();

        fields.add(routerField);
        methods.add(routeMethod);
    }

    private void generateBindRouterMethod(List<MethodSpec> methods) {
        if (!hasRoutingElements) return;

        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bindRouter")
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class);

        ClassName routerClassName = classNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String routerFieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);

        ParameterSpec routerParameter = ParameterSpec.builder(routerClassName, routerFieldName)
                .addAnnotation(NotNull.class)
                .build();

        bindMethodBuilder.addParameter(routerParameter);
        bindMethodBuilder.addStatement("this.$L = $L", routerFieldName, routerFieldName);
        bindMethodBuilder.addStatement("this.$L.attach()", routerFieldName);

        methods.add(bindMethodBuilder.build());
    }

    private void generateUnbindRouterMethod(List<MethodSpec> methods) {
        if (!hasRoutingElements) return;

        MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbindRouter")
                .addModifiers(PUBLIC, FINAL)
                .returns(void.class);

        ClassName routerClassName = classNames.getAppRouterClassName();
        String generatedRouterSimpleClassName = routerClassName.simpleName();
        String routerFieldName = MemberUtils.getFieldNameForClass(generatedRouterSimpleClassName);

        unbindMethodBuilder.addStatement("this.$L.detach()", routerFieldName);
        unbindMethodBuilder.addStatement("this.$L = null", routerFieldName);

        methods.add(unbindMethodBuilder.build());
    }
}
