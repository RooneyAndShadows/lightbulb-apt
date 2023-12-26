package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbParcelableDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

public class ParcelableGenerator extends CodeGenerator {
    private final String parcelablePackage;
    private final List<LightbulbParcelableDescription> parcelableDescriptions;

    public ParcelableGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        parcelablePackage = PackageNames.getParcelablePackage();
        parcelableDescriptions = annotationResultsRegistry.getParcelableDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        generateParcelableImplementations();
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasParcelableDescriptions();
    }

    private void generateParcelableImplementations() {
        parcelableDescriptions.forEach(parcelableDescription -> {
            ClassName instrumentedClassName = parcelableDescription.getInstrumentedClassName();
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(parcelableDescription, fields);
            generateCreatorField(parcelableDescription, fields);
            generateConstructorMethod(methods);
            generateDescribeContentsMethod(methods);
            generateWriteToParcelMethod(methods);

            TypeSpec.Builder parcelableClass = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .addSuperinterface(ANDROID_PARCELABLE)
                    .addFields(fields)
                    .addMethods(methods);


            try {
                JavaFile.builder(parcelablePackage, parcelableClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateConstructorMethod(List<MethodSpec> methods) {
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(PROTECTED)
                .addParameter(ANDROID_PARCEL, "in")
                .build();

        methods.add(constructorMethod);
    }

    private void generateDescribeContentsMethod(List<MethodSpec> methods) {
        MethodSpec describeContentsMethod = MethodSpec.methodBuilder("describeContents")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addStatement("return 0")
                .build();

        methods.add(describeContentsMethod);
    }

    private void generateWriteToParcelMethod(List<MethodSpec> methods) {
        ParameterSpec parcelParam = ParameterSpec.builder(ANDROID_PARCEL, "dest")
                .addAnnotation(NotNull.class)
                .build();

        MethodSpec writeToParcelMethod = MethodSpec.methodBuilder("writeToParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(parcelParam)
                .addParameter(TypeName.INT, "flags")
                .returns(void.class)
                .build();

        methods.add(writeToParcelMethod);
    }

    private void generateFields(LightbulbParcelableDescription parcelableDescription, List<FieldSpec> fields) {
        parcelableDescription.getFields().forEach(field -> {
            TypeName fieldTypeName = field.getTypeInformation().getTypeName();

            FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, field.getName(), PRIVATE)
                    .build();

            fields.add(fieldSpec);
        });
    }

    private void generateCreatorField(LightbulbParcelableDescription parcelableDescription, List<FieldSpec> fields) {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ANDROID_PARCELABLE_CREATOR, parcelableDescription.getInstrumentedClassName());

        TypeSpec.Builder creatorBuilder = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(typeName);

        MethodSpec createFromParcelMethod = MethodSpec.methodBuilder("createFromParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_PARCEL, "in")
                .returns(parcelableDescription.getInstrumentedClassName())
                .addStatement("return new $T(in)", parcelableDescription.getInstrumentedClassName())
                .build();

        ArrayTypeName returnType = ArrayTypeName.of(parcelableDescription.getInstrumentedClassName());

        MethodSpec newArrayMethod = MethodSpec.methodBuilder("newArray")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "size")
                .returns(returnType)
                .addStatement("return new $T[size]", parcelableDescription.getInstrumentedClassName())
                .build();

        creatorBuilder.addMethod(createFromParcelMethod);
        creatorBuilder.addMethod(newArrayMethod);

        TypeSpec creator = creatorBuilder.build();

        FieldSpec fieldSpec = FieldSpec.builder(typeName, "CREATOR", PUBLIC, STATIC, FINAL)
                .initializer("$L", creator)
                .build();

        fields.add(fieldSpec);
    }
}
