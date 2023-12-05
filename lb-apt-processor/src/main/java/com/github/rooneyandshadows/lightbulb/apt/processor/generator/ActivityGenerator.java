package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.activity.LightbulbActivityData;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS;
import static javax.lang.model.element.Modifier.*;

//TODO ADD ON BACK PRESSED METHOD
public class ActivityGenerator extends CodeGenerator {
    private final String ROUTER_FIELD_NAME = "router";

    public ActivityGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
    }

    @Override
    public void generate() {
        List<LightbulbActivityData> activityBindings = annotationResultsRegistry.getResult(ACTIVITY_BINDINGS);
        generateActivities(activityBindings);
    }

    private void generateActivities(List<LightbulbActivityData> activityBindings) {
        activityBindings.forEach(activityInfo -> {
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(activityInfo, fields);
            generateOnCreateMethod(activityInfo, methods);
            generateOnSaveInstanceStateMethod(activityInfo, methods);
            generateOnDestroyMethod(activityInfo, methods);

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(activityInfo.instrumentedClassName())
                    .addModifiers(PUBLIC, ABSTRACT)
                    .superclass(activityInfo.superClassName())
                    .addFields(fields)
                    .addMethods(methods);
            try {
                JavaFile.builder(activityInfo.instrumentedClassName().packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateFields(LightbulbActivityData activityBindingData, List<FieldSpec> destination) {
        boolean routingEnabled = activityBindingData.routingEnabled();

        if (routingEnabled) {
            ClassName appRouterClassName = ClassNames.getAppRouterClassName();
            FieldSpec fieldSpec = FieldSpec.builder(appRouterClassName, ROUTER_FIELD_NAME, PRIVATE).build();
            destination.add(fieldSpec);
        }
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnCreateMethod(LightbulbActivityData activityBindingData, List<MethodSpec> destination) {
        boolean routingEnabled = activityBindingData.routingEnabled();

        if (!routingEnabled) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (routingEnabled) {
            ClassName routerClassName = ClassNames.getAppRouterClassName();
            ClassName appNavigatorClassName = ClassNames.getAppNavigatorClassName();
            ClassName RClassName = ClassNames.androidResources();
            String fragmentContainerId = activityBindingData.fragmentContainerId();

            builder.addStatement("$T $L = $T.id.$L", ClassNames.INTEGER, "fragmentContainerId", RClassName, fragmentContainerId);
            builder.addStatement("$L = new $T($L,$L)", ROUTER_FIELD_NAME, routerClassName, "this", "fragmentContainerId");
            builder.beginControlFlow("if(savedInstanceState != null)")
                    .addStatement("$L.restoreState(savedInstanceState)", ROUTER_FIELD_NAME)
                    .endControlFlow();
            builder.addStatement("$L.getInstance().bind($L)", appNavigatorClassName, ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnSaveInstanceStateMethod(LightbulbActivityData activityBindingData, List<MethodSpec> destination) {
        boolean routingEnabled = activityBindingData.routingEnabled();

        if (!routingEnabled) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onSaveInstanceState")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "outState")
                .returns(void.class)
                .addStatement("super.onSaveInstanceState(outState)");

        if (routingEnabled) {
            builder.addStatement("$L.saveState(outState)", ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnDestroyMethod(LightbulbActivityData activityBindingData, List<MethodSpec> destination) {
        boolean routingEnabled = activityBindingData.routingEnabled();

        if (!routingEnabled) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onDestroy")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("super.onDestroy()");

        if (routingEnabled) {
            ClassName appNavigatorClassName = ClassNames.getAppNavigatorClassName();
            builder.addStatement("$L.getInstance().unBind()", appNavigatorClassName);
        }

        destination.add(builder.build());
    }
}
