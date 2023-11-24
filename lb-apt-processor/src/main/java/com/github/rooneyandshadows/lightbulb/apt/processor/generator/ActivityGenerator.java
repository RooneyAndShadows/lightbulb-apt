package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.BASE_ROUTER;
import static javax.lang.model.element.Modifier.*;

public class ActivityGenerator extends CodeGenerator {

    public ActivityGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
    }

    @Override
    public void generate() {
        List<ActivityBindingData> activityBindings = annotationResultsRegistry.getResult(ACTIVITY_BINDINGS);
        generateActivities(activityBindings);
    }

    private void generateActivities(List<ActivityBindingData> activityBindings) {
        activityBindings.forEach(activityInfo -> {
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(activityInfo, fields);
            generateOnCreateMethod(activityInfo, methods);

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(activityInfo.getInstrumentedClassName())
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .superclass(activityInfo.getSuperClassName())
                    .addMethods(methods);
            try {
                JavaFile.builder(activityInfo.getInstrumentedClassName().packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateFields(ActivityBindingData activityBindingData, List<FieldSpec> destination) {
        if (activityBindingData.isRoutingEnabled()) {
            FieldSpec fieldSpec = FieldSpec.builder(BASE_ROUTER, "router", PRIVATE).build();
            destination.add(fieldSpec);
        }
    }

    private void generateOnCreateMethod(ActivityBindingData activityBindingData, List<MethodSpec> destination) {
        if (!activityBindingData.isRoutingEnabled()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)")
                .beginControlFlow("if(savedInstanceState != null)")
                .addStatement("restoreVariablesState(savedInstanceState)")
                .endControlFlow();

        destination.add(builder.build());
    }
}
