package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata.ScreenParameterMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.*;
import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings("DuplicatedCode")
public class RoutingGenerator extends CodeGenerator {
    private final ClassName screensClassName;
    private final List<FragmentMetadata> fragmentMetadataList;

    public RoutingGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        screensClassName = classNames.getRoutingScreensClassName();
        fragmentMetadataList = annotationResultsRegistry.getFragmentDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        generateRoutingScreens();
        generateAppRouter();
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasRoutingScreens();
    }

    private void generateAppRouter() {
        ClassName routerClassName = classNames.getAppRouterClassName();
        List<TypeSpec> innerClasses = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();

        TypeSpec.Builder routerClassBuilder = TypeSpec
                .classBuilder(routerClassName)
                .superclass(ClassNameUtils.BASE_ROUTER)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassNameUtils.ANDROID_ACTIVITY, "contextActivity")
                        .addParameter(TypeName.INT, "fragmentContainerId")
                        .addStatement("super(contextActivity,fragmentContainerId)")
                        .build()
                );

        fragmentMetadataList.stream()
                .filter(FragmentMetadata::isScreen)
                .collect(groupingBy(FragmentMetadata::getScreenGroupName))
                .forEach((groupName, fragmentsInGroup) -> {
                    fragmentsInGroup.forEach(fragmentMetadata -> {
                        generateRouteClass(innerClasses, methods, fragmentMetadata, routerClassName);
                    });
                });

        routerClassBuilder.addTypes(innerClasses);
        routerClassBuilder.addMethods(methods);

        writeClassFile(routerClassName.packageName(), routerClassBuilder);
    }


    private void generateRoutingScreens() {
        TypeSpec.Builder rootClassBuilder = TypeSpec
                .classBuilder(ROUTING_SCREENS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fragmentMetadataList.stream()
                .filter(FragmentMetadata::isScreen)
                .collect(groupingBy(FragmentMetadata::getScreenGroupName))
                .forEach((screenGroup, fragments) -> {
                    TypeSpec screenGroupClass = generateScreenGroupClass(screenGroup, fragments);
                    rootClassBuilder.addType(screenGroupClass);
                });

        writeClassFile(screensClassName.packageName(), rootClassBuilder);
    }

    private void generateRouteClass(List<TypeSpec> classes, List<MethodSpec> methods, FragmentMetadata fragment, ClassName routerClassName) {
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
            optionalConstructor.addParameter(generateScreenParameterSpec(parameter));
            allParamsRouteMethod.addParameter(generateScreenParameterSpec(parameter));
        });
        allParamsRouteMethod.addStatement("return new $T($L)", routeClassName, allParamsString);
        optionalConstructor.addStatement("this.screen = new $T($L)", screenClass, allParamsString);

        if (notOptionalConstructor != null) {
            String notOptionalParams = generateCommaSeparatedParamsString(false, fragment, parameter -> {
                notOptionalConstructor.addParameter(generateScreenParameterSpec(parameter));
                requiredParamsRouteMethod.addParameter(generateScreenParameterSpec(parameter));
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

        if (hasOptionalParameters) {
            routeClassBuilder.addMethod(notOptionalConstructor.build());
        }

        classes.add(routeClassBuilder.build());
        methods.add(allParamsRouteMethod.build());

        if (hasOptionalParameters) {
            methods.add(requiredParamsRouteMethod.build());
        }
    }

    private TypeSpec generateScreenGroupClass(String screenGroupName, List<FragmentMetadata> fragmentMetadataList) {
        ClassName fragmentScreenClass = ClassNameUtils.BASE_ROUTER.nestedClass("FragmentScreen");

        TypeSpec.Builder groupClass = TypeSpec
                .classBuilder(screenGroupName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);

        fragmentMetadataList.forEach(fragmentInfo -> {
            ClassName fragmentClassName = classNames.getClassName(fragmentInfo);

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

            fragmentInfo.getScreenParameters().forEach(parameter -> {
                String parameterName = parameter.getName();
                TypeName parameterType = classNames.getTypeName(parameter);

                FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterType, parameterName, Modifier.PRIVATE);

                if (parameter.isOptional()) {
                    fieldSpecBuilder.initializer("null");
                }

                screenClass.addField(fieldSpecBuilder.build());
            });

            String allParamsString = generateCommaSeparatedParamsString(true, fragmentInfo, parameter -> {
                String parameterName = parameter.getName();

                optionalScreenConstructor.addParameter(generateScreenParameterSpec(parameter));
                optionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
            });

            screenClass.addMethod(optionalScreenConstructor.build());

            if (notOptionalScreenConstructor != null) {
                fragmentInfo.getScreenParameters(false).forEach(parameter -> {
                    String parameterName = parameter.getName();

                    if (!parameter.isOptional()) {
                        notOptionalScreenConstructor.addParameter(generateScreenParameterSpec(parameter));
                        notOptionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                    }
                });

                screenClass.addMethod(notOptionalScreenConstructor.build());
            }

            getFragmentMethod.addStatement("return $T.$L.newInstance(" + allParamsString + ")", classNames.getFragmentFactoryClassName(), fragmentClassName.simpleName());

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

    private String generateCommaSeparatedParamsString(boolean includeOptional, FragmentMetadata fragmentMetadata, Consumer<ScreenParameterMetadata> consumer) {
        String paramsString = "";
        List<ScreenParameterMetadata> collection = fragmentMetadata.getScreenParameters(includeOptional);
        for (int index = 0; index < collection.size(); index++) {
            ScreenParameterMetadata param = collection.get(index);
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