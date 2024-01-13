package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ActivityMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.*;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("unused")
public class ActivityGenerator extends CodeGenerator {
    private final String ROUTER_FIELD_NAME = "router";
    private final boolean hasRouter;
    private final List<ActivityMetadata> activityMetadataList;

    public ActivityGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        hasRouter = annotationResultsRegistry.hasRoutingScreens();
        activityMetadataList = annotationResultsRegistry.getActivityDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        activityMetadataList.forEach(activityMetadata -> {
            ClassName activitySuperClassName = getSuperClassName(activityMetadata);
            ClassName instrumentedClassName = getInstrumentedClassName(packageNames.getActivitiesPackage(), activityMetadata);

            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(fields);
            generateOnCreateMethod(activityMetadata, methods);
            generateOnSaveInstanceStateMethod(methods);
            generateOnDestroyMethod(methods);

            TypeSpec.Builder activityClassBuilder = TypeSpec.classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .addMethods(methods);

            if (activitySuperClassName != null) {
                activityClassBuilder.superclass(activitySuperClassName);
            }

            writeClassFile(instrumentedClassName.packageName(), activityClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasActivityDescriptions();
    }

    private void generateFields(List<FieldSpec> destination) {
        if (hasRouter) {
            ClassName appRouterClassName = classNames.getAppRouterClassName();
            FieldSpec fieldSpec = FieldSpec.builder(appRouterClassName, ROUTER_FIELD_NAME, PRIVATE).build();
            destination.add(fieldSpec);
        }
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnCreateMethod(ActivityMetadata activityMetadata, List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (hasRouter) {
            ClassName routerClassName = classNames.getAppRouterClassName();
            ClassName appNavigatorClassName = classNames.getLightbulbServiceClassName();
            ClassName RClassName = classNames.androidResources();
            String fragmentContainerId = activityMetadata.getFragmentContainerId();

            builder.addStatement("$T $L = $T.id.$L", INTEGER, "fragmentContainerId", RClassName, fragmentContainerId);
            builder.addStatement("$L = new $T($L,$L)", ROUTER_FIELD_NAME, routerClassName, "this", "fragmentContainerId");
            builder.beginControlFlow("if(savedInstanceState != null)")
                    .addStatement("$L.restoreState(savedInstanceState)", ROUTER_FIELD_NAME)
                    .endControlFlow();
            builder.addStatement("$L.getInstance().bindRouter($L)", appNavigatorClassName, ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnSaveInstanceStateMethod(List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onSaveInstanceState")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_BUNDLE, "outState")
                .returns(void.class)
                .addStatement("super.onSaveInstanceState(outState)");

        if (hasRouter) {
            builder.addStatement("$L.saveState(outState)", ROUTER_FIELD_NAME);
        }

        destination.add(builder.build());
    }

    @SuppressWarnings("ConstantValue")
    private void generateOnDestroyMethod(List<MethodSpec> destination) {
        if (!hasRouter) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onDestroy")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("super.onDestroy()");

        if (hasRouter) {
            ClassName appNavigatorClassName = classNames.getLightbulbServiceClassName();
            builder.addStatement("$L.getInstance().unbindRouter()", appNavigatorClassName);
        }

        destination.add(builder.build());
    }
}
