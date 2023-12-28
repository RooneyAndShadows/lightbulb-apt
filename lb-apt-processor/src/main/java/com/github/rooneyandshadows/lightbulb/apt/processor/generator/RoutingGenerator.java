package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.ROUTING_SCREENS_CLASS_NAME;

@SuppressWarnings("DuplicatedCode")
public class RoutingGenerator extends CodeGenerator {
    private final ClassName screensClassName;

    public RoutingGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer,elements, annotationResultsRegistry);
        screensClassName = ClassNames.getRoutingScreensClassName();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbFragmentDescription> fragmentBindings = annotationResultsRegistry.getFragmentDescriptions();

        generateRoutingScreens(fragmentBindings);
        generateAppRouter(fragmentBindings);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasRoutingScreens();
    }

    private void generateAppRouter(List<LightbulbFragmentDescription> fragmentBindings) {
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
                .filter(LightbulbFragmentDescription::isScreen)
                .collect(Collectors.groupingBy(LightbulbFragmentDescription::getScreenGroupName))
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


    private void generateRoutingScreens(List<LightbulbFragmentDescription> fragmentBindings) {
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(ROUTING_SCREENS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentBindings.stream()
                .filter(LightbulbFragmentDescription::isScreen)
                .collect(Collectors.groupingBy(LightbulbFragmentDescription::getScreenGroupName))
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

    private void generateRouteClass(TypeSpec.Builder routerClass, LightbulbFragmentDescription fragment, ClassName routerClassName) {
        boolean hasOptionalParameters = fragment.hasOptionalParameters();
        String groupName = fragment.getScreenGroupName();
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

        String allParamsString = generateCommaSeparatedParamsString(true, fragment, parameter -> {
            optionalConstructor.addParameter(generateParameterSpec(parameter));
            allParamsRouteMethod.addParameter(generateParameterSpec(parameter));
        });
        allParamsRouteMethod.addStatement("return new $T($L)", routeClassName, allParamsString);
        optionalConstructor.addStatement("this.screen = new $T($L)", screenClass, allParamsString);

        if (notOptionalConstructor != null) {
            String notOptionalParams = generateCommaSeparatedParamsString(false, fragment, parameter -> {
                notOptionalConstructor.addParameter(generateParameterSpec(parameter));
                requiredParamsRouteMethod.addParameter(generateParameterSpec(parameter));
            });
            notOptionalConstructor.addStatement("this.screen = new $T($L)", screenClass, notOptionalParams);
            requiredParamsRouteMethod.addStatement("return new $T($L)", routeClassName, notOptionalParams);
        }

        routeClassBuilder.addField(screenClass, "screen", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(optionalConstructor.build())
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

    private TypeSpec generateScreenGroupClass(String screenGroupName, List<LightbulbFragmentDescription> fragments) {
        ClassName fragmentScreenClass = ClassNames.BASE_ROUTER.nestedClass("FragmentScreen");

        TypeSpec.Builder groupClass = TypeSpec
                .classBuilder(screenGroupName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);

        fragments.forEach(fragmentInfo -> {
            ClassName fragmentClassName = fragmentInfo.getClassName();
            boolean hasOptionalParameters = fragmentInfo.hasOptionalParameters();
            String screenName = fragmentInfo.getScreenName();

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
                    .returns(fragmentClassName);

            fragmentInfo.getScreenParameterFields().forEach(paramInfo -> {
                String parameterName = paramInfo.getName();
                TypeName parameterType = paramInfo.getTypeInformation().getTypeName();
                FieldSpec.Builder field = FieldSpec.builder(parameterType, parameterName, Modifier.PRIVATE);

                if (paramInfo.isOptional()) {
                    field.initializer("null");
                }

                screenClass.addField(field.build());
            });

            String allParamsString = generateCommaSeparatedParamsString(true, fragmentInfo, parameter -> {
                String parameterName = parameter.getName();
                optionalScreenConstructor.addParameter(generateParameterSpec(parameter));
                optionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
            });

            screenClass.addMethod(optionalScreenConstructor.build());

            if (notOptionalScreenConstructor != null) {
                fragmentInfo.getFragmentParameters(false).forEach(parameter -> {
                    String parameterName = parameter.getName();

                    if (!parameter.isOptional()) {
                        notOptionalScreenConstructor.addParameter(generateParameterSpec(parameter));
                        notOptionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                    }
                });

                screenClass.addMethod(notOptionalScreenConstructor.build());
            }

            getFragmentMethod.addStatement("return $T.$L.newInstance(" + allParamsString + ")", ClassNames.getFragmentFactoryClassName(), fragmentClassName.simpleName());

            screenClass.addMethod(getFragmentMethod.build());

            groupClass.addType(screenClass.build());
        });
        return groupClass.build();
    }

    private MethodSpec generateRouteToNewRootScreenMethodForScreen(String routerClassName) {
        return MethodSpec.methodBuilder("newRootScreen")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("$L.newRootScreen(screen)", routerClassName.concat(".this"))
                .build();
    }

    private MethodSpec generateRouteBackNTimesAndReplaceMethodForScreen(String routerClassName) {
        return MethodSpec.methodBuilder("backAndReplace")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.INT, "backNTimes")
                .addStatement("$L.backNTimesAndReplace(backNTimes,screen)", routerClassName.concat(".this"))
                .build();
    }

    private MethodSpec generateRouteReplaceMethodForScreen(String routerClassName) {
        return MethodSpec.methodBuilder("replace")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("$L.replaceTop(screen)", routerClassName.concat(".this"))
                .build();
    }

    private MethodSpec generateRouteForwardMethodForScreen(String routerClassName) {
        return MethodSpec.methodBuilder("forward")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("$L.forward(screen)", routerClassName.concat(".this"))
                .build();
    }

    private String generateCommaSeparatedParamsString(boolean includeOptional, LightbulbFragmentDescription fragmentData, Consumer<FieldScreenParameter> consumer) {
        String paramsString = "";
        List<FieldScreenParameter> collection = fragmentData.getFragmentParameters(includeOptional);
        for (int index = 0; index < collection.size(); index++) {
            FieldScreenParameter param = collection.get(index);
            boolean isLast = index == collection.size() - 1;
            consumer.accept(param);
            paramsString = paramsString.concat(param.getName());
            if (!isLast) {
                paramsString = paramsString.concat(", ");
            }
        }
        return paramsString;
    }
}