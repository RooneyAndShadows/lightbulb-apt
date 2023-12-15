package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.StringUtils;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.AnnotationResultUtils.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.AnnotationResultUtils.hasRoutingScreens;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.AnnotationResultUtils.hasStorageElements;

public class ServiceGenerator extends CodeGenerator {
    private final String servicePackage;
    private final boolean hasRoutingElements;
    private final boolean hasStorageElements;

    public ServiceGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        servicePackage = PackageNames.getServicePackage();
        hasRoutingElements = hasRoutingScreens(annotationResultsRegistry);
        hasStorageElements = hasStorageElements(annotationResultsRegistry);
    }

    @Override
    public void generate() {
        if (!hasStorageElements && !hasRoutingElements) {
            return;
        }

        generateServiceSingleton();
    }

    private void generateStorages(TypeSpec.Builder singletonBuilder) {
        List<LightbulbStorageDescription> storageDescriptions = getStorageDescriptions(annotationResultsRegistry);
        storageDescriptions.forEach(storageDescription -> {
            ClassName generatedStorageClassName = storageDescription.getInstrumentedClassName();
            String fieldName = StringUtils.lowerCaseFirstLetter(generatedStorageClassName.simpleName());
            String getterName = String.format("get%s", generatedStorageClassName.simpleName());

            FieldSpec storageField = FieldSpec.builder(generatedStorageClassName, fieldName, Modifier.PRIVATE)
                    .build();

            MethodSpec storageGetter = MethodSpec.methodBuilder(getterName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(generatedStorageClassName)
                    .addStatement("return fieldName")
                    .build();

            singletonBuilder.addField(storageField);
            singletonBuilder.addMethod(storageGetter);
        });
    }

    private void generateRouter(TypeSpec.Builder singletonBuilder) {

    }

    private void generateServiceSingleton() {
        ClassName routerClassName = ClassNames.getAppRouterClassName();
        ClassName lightbulbServiceClassName = ClassNames.getLightbulbServiceClassName();
        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(lightbulbServiceClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(lightbulbServiceClassName, "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addField(routerClassName, "router", Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .returns(lightbulbServiceClassName)
                        .beginControlFlow("if(instance == null)")
                        .addStatement("instance = new $T()", lightbulbServiceClassName)
                        .endControlFlow()
                        .addStatement("return instance")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("getRouter")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(routerClassName)
                        .addStatement("return router")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("route")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(routerClassName)
                        .addStatement("return getInstance().getRouter()")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(routerClassName, "router")
                        .returns(void.class)
                        .addStatement("this.router = router")
                        .addStatement("this.router.attach()")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("unBind")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addStatement("this.router.detach()")
                        .addStatement("this.router = null")
                        .build()
                );
        try {
            JavaFile.builder(lightbulbServiceClassName.packageName(), singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }


}
