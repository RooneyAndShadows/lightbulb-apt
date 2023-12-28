package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbActivityDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

public class ActivityGenerator extends CodeGenerator {
    private final String ROUTER_FIELD_NAME = "router";
    private final boolean hasRouter;
    private final boolean hasStorage;

    public ActivityGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, annotationResultsRegistry);
        hasRouter = annotationResultsRegistry.hasRoutingScreens();
        hasStorage = annotationResultsRegistry.hasStorageDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbActivityDescription> activityDescriptions = annotationResultsRegistry.getActivityDescriptions();

        generateActivities(activityDescriptions);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasActivityDescriptions();
    }


    private void generateActivities(List<LightbulbActivityDescription> activityBindings) {
        activityBindings.forEach(activityInfo -> {
            ClassName instrumentedClassName = activityInfo.getInstrumentedClassName();
            ClassName superclassName = activityInfo.getSuperClassName();

            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(activityInfo, fields);
            generateOnCreateMethod(activityInfo, methods);
            generateOnSaveInstanceStateMethod(activityInfo, methods);
            generateOnDestroyMethod(activityInfo, methods);

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

    private void generateFields(LightbulbActivityDescription activityBindingData, List<FieldSpec> destination) {
        if (hasRouter) {
            ClassName appRouterClassName = ClassNames.getAppRouterClassName();
            FieldSpec fieldSpec = FieldSpec.builder(appRouterClassName, ROUTER_FIELD_NAME, PRIVATE).build();
            destination.add(fieldSpec);
        }
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnCreateMethod(LightbulbActivityDescription activityBindingData, List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (hasRouter) {
            ClassName routerClassName = ClassNames.getAppRouterClassName();
            ClassName appNavigatorClassName = ClassNames.getLightbulbServiceClassName();
            ClassName RClassName = ClassNames.androidResources();
            String fragmentContainerId = activityBindingData.getFragmentContainerId();

            builder.addStatement("$T $L = $T.id.$L", ClassNames.INTEGER, "fragmentContainerId", RClassName, fragmentContainerId);
            builder.addStatement("$L = new $T($L,$L)", ROUTER_FIELD_NAME, routerClassName, "this", "fragmentContainerId");
            builder.beginControlFlow("if(savedInstanceState != null)")
                    .addStatement("$L.restoreState(savedInstanceState)", ROUTER_FIELD_NAME)
                    .endControlFlow();
            builder.addStatement("$L.getInstance().bindRouter($L)", appNavigatorClassName, ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnSaveInstanceStateMethod(LightbulbActivityDescription activityBindingData, List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onSaveInstanceState")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "outState")
                .returns(void.class)
                .addStatement("super.onSaveInstanceState(outState)");

        if (hasRouter) {
            builder.addStatement("$L.saveState(outState)", ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnDestroyMethod(LightbulbActivityDescription activityBindingData, List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onDestroy")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("super.onDestroy()");

        if (hasRouter) {
            ClassName appNavigatorClassName = ClassNames.getLightbulbServiceClassName();
            builder.addStatement("$L.getInstance().unbindRouter()", appNavigatorClassName);
        }

        destination.add(builder.build());
    }
}
