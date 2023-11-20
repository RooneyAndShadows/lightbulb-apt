package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.BASE_ACTIVITY;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.BASE_ROUTER;

@SuppressWarnings("DuplicatedCode")
public class RoutingGenerator extends CodeGenerator {
    private final String screensPackage;
    private final ClassName screensClassName;

    public RoutingGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        screensPackage = PackageNames.getRoutingScreensPackage();
        screensClassName = ClassName.get(screensPackage, "Screens");
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        List<ActivityBindingData> activityBindings = annotationResultsRegistry.getResult(ACTIVITY_BINDINGS);
        generateRoutingScreens(fragmentBindings);
        activityBindings.stream()
                .filter(ActivityBindingData::isRoutingEnabled)
                .forEach(activityInfo -> {
                    generateRouterClass(activityInfo.getClassName(), fragmentBindings);
                });
    }

    private void generateActivityNavigatorSingleton(ClassName activityClassName, ClassName routerClassName) {
        ClassName navigatorClassName = ClassName.get("", activityClassName.simpleName().concat("Navigator"));
        String navigatorPackage = activityClassName.packageName();
        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(navigatorClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(navigatorClassName, "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addField(routerClassName, "router", Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .returns(navigatorClassName)
                        .beginControlFlow("if(instance == null)")
                        .addStatement("instance = new $T()", navigatorClassName)
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
                ).addMethod(MethodSpec.methodBuilder("initializeRouter")
                        .addParameter(BASE_ACTIVITY, "activity")
                        .addParameter(int.class, "fragmentContainerId")
                        .returns(routerClassName)
                        .addStatement("this.router = new $T($L,$L)", routerClassName, "activity", "fragmentContainerId")
                        .addStatement("return this.router")
                        .build()
                ).addMethod(MethodSpec.methodBuilder("unBind")
                        .returns(void.class)
                        .addStatement("this.router = null")
                        .build()
                );
        try {
            JavaFile.builder(navigatorPackage, singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateRouterClass(ClassName activityClassName, List<FragmentBindingData> fragmentBindings) {
        String routerPackage = activityClassName.packageName();
        ClassName routerClassName = ClassName.get(routerPackage, activityClassName.simpleName().concat("Router"));
        TypeSpec.Builder routerClass = TypeSpec
                .classBuilder(routerClassName)
                .superclass(BASE_ROUTER)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(BASE_ACTIVITY, "contextActivity")
                        .addParameter(TypeName.INT, "fragmentContainerId")
                        .addStatement("super(contextActivity,fragmentContainerId)")
                        .build()
                );
        fragmentBindings.stream()
                .filter(fragmentBindingData -> fragmentBindingData.getScreenInfo() != null)
                .collect(Collectors.groupingBy(fragmentBindingData -> fragmentBindingData.getScreenInfo().getScreenGroupName()))
                .forEach((groupName, fragmentsInGroup) -> {
                    fragmentsInGroup.forEach(fragmentBinding -> {
                        generateRouteClass(routerClass, fragmentBinding, routerClassName);
                    });
                });
        try {
            JavaFile.builder(routerPackage, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        generateActivityNavigatorSingleton(activityClassName, routerClassName);
    }

    private void generateRoutingScreens(List<FragmentBindingData> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder("Screens")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentBindings.stream()
                .filter(fragmentBindingData -> fragmentBindingData.getScreenInfo() != null)
                .collect(Collectors.groupingBy(fragmentBindingData -> fragmentBindingData.getScreenInfo().getScreenGroupName()))
                .forEach((screenGroup, fragments) -> {
                    TypeSpec screenGroupClass = generateScreenGroupClass(screenGroup, fragments);
                    rootClass.addType(screenGroupClass);
                });
        try {
            JavaFile.builder(screensPackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateRouteClass(TypeSpec.Builder routerClass, FragmentBindingData fragment, ClassName routerClassName) {
        boolean hasOptionalParameters = fragment.hasOptionalParameters();
        String groupName = fragment.getScreenInfo().getScreenGroupName();
        String screenName = fragment.getScreenInfo().getScreenName();
        String screenClassName = groupName.concat(screenName);
        ClassName groupClass = screensClassName.nestedClass(groupName);
        ClassName screenClass = groupClass.nestedClass(screenName);
        ClassName routeClassName = ClassName.get("", screenClassName);
        TypeSpec.Builder routeClassBuilder = TypeSpec.classBuilder(screenClassName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);
        MethodSpec.Builder notOptionalConstructor = !hasOptionalParameters ? null : MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        MethodSpec.Builder optionalConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        MethodSpec.Builder allParamsRouteMethod = MethodSpec.methodBuilder("to".concat(routeClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(routeClassName);
        MethodSpec.Builder requiredParamsRouteMethod = !hasOptionalParameters ? null : MethodSpec
                .methodBuilder("to".concat(routeClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(routeClassName);
        String allParams = fragment.generateCommaSeparatedParams(true, parameter -> {
            optionalConstructor.addParameter(parameter.getParameterSpec());
            allParamsRouteMethod.addParameter(parameter.getParameterSpec());
        });
        allParamsRouteMethod.addStatement("return new $T($L)", routeClassName, allParams);
        optionalConstructor.addStatement("this.screen = new $T($L)", screenClass, allParams);
        if (notOptionalConstructor != null) {
            String notOptionalParams = fragment.generateCommaSeparatedParams(false, parameter -> {
                notOptionalConstructor.addParameter(parameter.getParameterSpec());
                requiredParamsRouteMethod.addParameter(parameter.getParameterSpec());
            });
            notOptionalConstructor.addStatement("this.screen = new $T($L)", screenClass, notOptionalParams);
            requiredParamsRouteMethod.addStatement("return new $T($L)", routeClassName, notOptionalParams);
        }
        routeClassBuilder.addField(screenClass, "screen", Modifier.PRIVATE, Modifier.FINAL);
        routeClassBuilder.addMethod(optionalConstructor.build())
                .addMethod(generateRouteForwardMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteReplaceMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteBackNTimesAndReplaceMethodForScreen(routerClassName.simpleName()))
                .addMethod(generateRouteToNewRootScreenMethodForScreen(routerClassName.simpleName()));
        if (hasOptionalParameters)
            routeClassBuilder.addMethod(notOptionalConstructor.build());
        routerClass.addMethod(allParamsRouteMethod.build());
        if (hasOptionalParameters)
            routerClass.addMethod(requiredParamsRouteMethod.build());
        routerClass.addType(routeClassBuilder.build());
    }

    private TypeSpec generateScreenGroupClass(String screenGroupName, List<FragmentBindingData> fragments) {
        ClassName fragmentScreenClass = BASE_ROUTER.nestedClass("FragmentScreen");
        TypeSpec.Builder groupClass = TypeSpec
                .classBuilder(screenGroupName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
        fragments.forEach(fragmentInfo -> {
            boolean hasOptionalParameters = fragmentInfo.hasOptionalParameters();
            String screenName = fragmentInfo.getScreenInfo().getScreenName();
            MethodSpec.Builder optionalScreenConstructor = MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            MethodSpec.Builder notOptionalScreenConstructor = !hasOptionalParameters ? null : MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            TypeSpec.Builder screenClass = TypeSpec
                    .classBuilder(screenName)
                    .superclass(fragmentScreenClass)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            MethodSpec.Builder getFragmentMethod = MethodSpec
                    .methodBuilder("getFragment")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(fragmentInfo.getClassName());
            fragmentInfo.getParameters().forEach(paramInfo -> {
                String parameterName = paramInfo.getName();
                TypeName parameterType = paramInfo.getType();
                FieldSpec.Builder field = FieldSpec.builder(parameterType, parameterName, Modifier.PRIVATE);
                if (paramInfo.isOptional())
                    field.initializer("null");
                screenClass.addField(field.build());
            });
            String allParams = fragmentInfo.generateCommaSeparatedParams(true, paramInfo -> {
                String parameterName = paramInfo.getName();
                optionalScreenConstructor.addParameter(paramInfo.getParameterSpec());
                optionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
            });
            screenClass.addMethod(optionalScreenConstructor.build());
            if (notOptionalScreenConstructor != null) {
                fragmentInfo.getFragmentParameters(false).forEach(paramInfo -> {
                    String parameterName = paramInfo.getName();
                    if (!paramInfo.isOptional()) {
                        notOptionalScreenConstructor.addParameter(paramInfo.getParameterSpec());
                        notOptionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                    }
                });
                screenClass.addMethod(notOptionalScreenConstructor.build());
            }
            getFragmentMethod.addStatement("return $T.$L.newInstance(" + allParams + ")", ClassNames.getFragmentFactoryClassName(), fragmentInfo.getSimpleClassName());
            screenClass.addMethod(getFragmentMethod.build());
            groupClass.addType(screenClass.build());
        });
        return groupClass.build();
    }

    private MethodSpec generateRouteToNewRootScreenMethodForScreen(String routerClassName) {
        String methodName = "newRootScreen";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.newRootScreen(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteBackNTimesAndReplaceMethodForScreen(String routerClassName) {
        String methodName = "backAndReplace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.INT, "backNTimes");
        methodBuilder.addStatement("$L.backNTimesAndReplace(backNTimes,screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteReplaceMethodForScreen(String routerClassName) {
        String methodName = "replace";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.replaceTop(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }

    private MethodSpec generateRouteForwardMethodForScreen(String routerClassName) {
        String methodName = "forward";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        methodBuilder.addStatement("$L.forward(screen)", routerClassName.concat(".this"));
        return methodBuilder.build();
    }
}
