package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner;

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.FragmentBindingData;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;

public class ScreenGroup {
    private final String screenGroupName;
    private final ArrayList<FragmentBindingData> screens = new ArrayList<>();

    public ScreenGroup(String screenGroupName) {
        if (screenGroupName == null || screenGroupName.equals(""))
            screenGroupName = "Common";
        this.screenGroupName = screenGroupName;
    }

    public String getScreenGroupName() {
        return screenGroupName;
    }

    public ArrayList<FragmentBindingData> getScreens() {
        return screens;
    }

    public void addScreen(FragmentBindingData fragmentInfo) {
        screens.add(fragmentInfo);
    }

    public TypeSpec build() {
        ClassName baseRouterClass = ClassName.get("com.github.rooneyandshadows.lightbulb.application.activity.routing", "BaseActivityRouter");
        ClassName fragmentScreenClass = baseRouterClass.nestedClass("FragmentScreen");
        TypeSpec.Builder groupClass = TypeSpec
                .classBuilder(screenGroupName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
        screens.forEach(fragmentInfo -> {
            boolean hasOptionalParameters = fragmentInfo.hasOptionalParameters();
            MethodSpec.Builder optionalScreenConstructor = MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            MethodSpec.Builder notOptionalScreenConstructor = !hasOptionalParameters ? null : MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            TypeSpec.Builder screenClass = TypeSpec
                    .classBuilder(fragmentInfo.getScreenName())
                    .superclass(fragmentScreenClass)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            MethodSpec.Builder getFragmentMethod = MethodSpec
                    .methodBuilder("getFragment")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(fragmentInfo.getClassName());
            fragmentInfo.getParameters().forEach(paramInfo -> {
                String parameterName = paramInfo.name;
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
            getFragmentMethod.addStatement("return $T.newInstance(" + allParams + ")", fragmentInfo.getBindingClassName());
            screenClass.addMethod(getFragmentMethod.build());
            groupClass.addType(screenClass.build());
        });
        return groupClass.build();
    }
}