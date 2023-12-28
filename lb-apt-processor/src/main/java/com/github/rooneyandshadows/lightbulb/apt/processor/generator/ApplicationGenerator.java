package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbApplicationDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.MemberUtils;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

public class ApplicationGenerator extends CodeGenerator {
    private final boolean hasStorages;
    private final boolean hasApplications;
    private final List<LightbulbApplicationDescription> appDescriptions;
    private final List<LightbulbStorageDescription> storageDescriptions;

    public ApplicationGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer,elements, annotationResultsRegistry);
        hasStorages = annotationResultsRegistry.hasStorageDescriptions();
        hasApplications = annotationResultsRegistry.hasApplicationDescriptions();
        appDescriptions = annotationResultsRegistry.getApplicationDescriptions();
        storageDescriptions = annotationResultsRegistry.getStorageDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        appDescriptions.forEach(applicationDescription -> {
            ClassName instrumentedClassName = applicationDescription.getInstrumentedClassName();
            ClassName superclassName = applicationDescription.getSuperClassName();

            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(fields);
            generateOnCreateMethod(methods);

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .superclass(superclassName)
                    .addFields(fields)
                    .addMethods(methods);
            try {
                JavaFile.builder(instrumentedClassName.packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return hasApplications;
    }

    private void generateFields(List<FieldSpec> destination) {
        if (hasStorages) {
            storageDescriptions.forEach(storageDescription -> {
                ClassName storageClassName = storageDescription.getInstrumentedClassName();
                String fieldName = MemberUtils.getFieldNameForClass(storageClassName.simpleName());
                FieldSpec fieldSpec = FieldSpec.builder(storageClassName, fieldName, PRIVATE).build();
                destination.add(fieldSpec);
            });
        }
    }

    private void generateOnCreateMethod(List<MethodSpec> destination) {
        if (!hasStorages) {
            return;
        }

        ClassName lbServiceClassName = ClassNames.getLightbulbServiceClassName();

        MethodSpec onCreateMethod = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("super.onCreate()")
                .addStatement("$L.getInstance().setApplicationContext(this)", lbServiceClassName)
                .build();


        destination.add(onCreateMethod);
    }
}
