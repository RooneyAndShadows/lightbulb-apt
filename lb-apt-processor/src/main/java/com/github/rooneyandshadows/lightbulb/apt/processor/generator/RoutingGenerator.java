package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.ROUTING_SCREENS_CLASS_NAME;

@SuppressWarnings("DuplicatedCode")
public class RoutingGenerator extends CodeGenerator {
    private final ClassName screensClassName;

    public RoutingGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        screensClassName = ClassNames.getRoutingScreensClassName();
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS);
        List<ActivityBindingData> activityBindings = annotationResultsRegistry.getResult(AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS);

        if (!hasScreens(fragmentBindings) && !hasRoutingEnabled(activityBindings)) {
            return;
        }

        generateRoutingScreens(fragmentBindings);
        generateAppRouter(fragmentBindings);
        generateAppNavigatorSingleton();
    }

    private void generateAppRouter(List<FragmentBindingData> fragmentBindings) {
        ClassName routerClassName = ClassNames.getAppRouterClassName();
        TypeSpec.Builder routerClass = TypeSpec
                .classBuilder(routerClassName)
                .superclass(ClassNames.BASE_ROUTER)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassNames.ANDROID_ACTIVITY, "contextActivity")
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
            JavaFile.builder(routerClassName.packageName(), routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateAppNavigatorSingleton() {
        ClassName routerClassName = ClassNames.getAppRouterClassName();
        ClassName navigatorClassName = ClassNames.getAppNavigatorClassName();
        TypeSpec.Builder singletonClass = TypeSpec
                .classBuilder(navigatorClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
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
            JavaFile.builder(navigatorClassName.packageName(), singletonClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateRoutingScreens(List<FragmentBindingData> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(ROUTING_SCREENS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentBindings.stream()
                .filter(fragmentBindingData -> fragmentBindingData.getScreenInfo() != null)
                .collect(Collectors.groupingBy(fragmentBindingData -> fragmentBindingData.getScreenInfo().getScreenGroupName()))
                .forEach((screenGroup, fragments) -> {
                    TypeSpec screenGroupClass = generateScreenGroupClass(screenGroup, fragments);
                    rootClass.addType(screenGroupClass);
                });
        try {
            JavaFile.builder(screensClassName.packageName(), rootClass.build()).build().writeTo(filer);
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
        ClassName fragmentScreenClass = ClassNames.BASE_ROUTER.nestedClass("FragmentScreen");
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

    private boolean hasScreens(List<FragmentBindingData> fragmentBindings) {
        return fragmentBindings.stream()
                .anyMatch(fragmentBindingData -> fragmentBindingData.getScreenInfo() != null);
    }

    private boolean hasRoutingEnabled(List<ActivityBindingData> activityBindings) {
        return activityBindings.stream()
                .anyMatch(ActivityBindingData::isRoutingEnabled);
    }
}
