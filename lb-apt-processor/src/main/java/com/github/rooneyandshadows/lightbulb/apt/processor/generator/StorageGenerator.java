package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_STORAGE_DESCRIPTION;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.BASE_STORAGE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class StorageGenerator extends CodeGenerator {
    private final String storagePackage;

    public StorageGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        storagePackage = PackageNames.getStoragePackage();
    }

    @Override
    public void generate() {
        List<LightbulbStorageDescription> storageDescriptions = annotationResultsRegistry.getResult(LIGHTBULB_STORAGE_DESCRIPTION);
        storageDescriptions.forEach(storageDescription -> {
            ClassName instrumentedClassName = storageDescription.getInstrumentedClassName();

            ParameterizedTypeName superClass = ParameterizedTypeName.get(
                    BASE_STORAGE,
                    storageDescription.getClassName());

            TypeSpec.Builder rootClass = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(superClass);

            try {
                JavaFile.builder(storagePackage, rootClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }
}
