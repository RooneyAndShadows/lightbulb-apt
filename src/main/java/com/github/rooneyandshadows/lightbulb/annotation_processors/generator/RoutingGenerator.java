package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentVariableInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.TypeUtils.*;

@SuppressWarnings("DuplicatedCode")
public class RoutingGenerator {
    private final Filer filer;
    private final String routingPackage;
    private final String screensPackage;
    private final ClassName screensClassName;

    public RoutingGenerator(String rootPackage, Filer filer) {
        this.filer = filer;
        routingPackage = rootPackage.concat(".routing");
        screensPackage = routingPackage.concat(".screens");
        screensClassName = ClassName.get(screensPackage, "Screens");
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

    public void generateRouterClass(ClassName activityClassName, List<FragmentScreenGroup> screenGroups) {
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

        screenGroups.forEach(group -> {
            group.getScreens().forEach(fragment -> {
                generateRouteClass(routerClass, fragment, group, routerClassName);
            });
        });
        try {
            JavaFile.builder(routerPackage, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        generateActivityNavigatorSingleton(activityClassName, routerClassName);
    }

    private void generateRouteClass(TypeSpec.Builder routerClass, FragmentInfo fragment, FragmentScreenGroup fragmentGroup, ClassName routerClassName) {
        boolean hasOptionalParameters = fragment.hasOptionalParameters();
        String groupName = fragmentGroup.getScreenGroupName();
        String screenName = fragment.getScreenName();
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


    public void generateRoutingScreens(List<FragmentScreenGroup> screenGroups) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder("Screens")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        screenGroups.forEach(group -> rootClass.addType(group.build()));
        try {
            JavaFile.builder(screensPackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
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
