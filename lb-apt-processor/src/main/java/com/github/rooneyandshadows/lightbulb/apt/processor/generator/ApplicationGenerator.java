package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ApplicationMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

@SuppressWarnings("DuplicatedCode")
public class ApplicationGenerator extends CodeGenerator {
    private final boolean hasStorages;
    private final boolean hasApplications;
    private final List<ApplicationMetadata> applicationMetadataList;

    public ApplicationGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        hasStorages = annotationResultsRegistry.hasStorageDescriptions();
        hasApplications = annotationResultsRegistry.hasApplicationDescriptions();
        applicationMetadataList = annotationResultsRegistry.getApplicationDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        applicationMetadataList.forEach(applicationMetadata -> {
            ClassName applicationSuperClassName = getSuperClassName(applicationMetadata);
            ClassName instrumentedClassName = getInstrumentedClassName(packageNames.getApplicationPackage(), applicationMetadata);
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateOnCreateMethod(methods);

            TypeSpec.Builder applicationClassBuilder = TypeSpec.classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .addMethods(methods);

            if (applicationSuperClassName != null) {
                applicationClassBuilder.superclass(applicationSuperClassName);
            }

            writeClassFile(instrumentedClassName.packageName(), applicationClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return hasApplications;
    }

    private void generateOnCreateMethod(List<MethodSpec> destination) {
        if (!hasStorages) {
            return;
        }

        ClassName lbServiceClassName = classNames.getLightbulbServiceClassName();

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
