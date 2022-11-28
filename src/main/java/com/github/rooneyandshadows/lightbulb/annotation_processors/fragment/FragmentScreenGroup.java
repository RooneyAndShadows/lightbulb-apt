package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.function.Consumer;

public class FragmentScreenGroup {
    private final String screenGroupName;
    private final ArrayList<FragmentInfo> screens = new ArrayList<>();

    public FragmentScreenGroup(String screenGroupName) {
        if (screenGroupName == null || screenGroupName.equals(""))
            screenGroupName = "Common";
        this.screenGroupName = screenGroupName;
    }

    public String getScreenGroupName() {
        return screenGroupName;
    }

    public ArrayList<FragmentInfo> getScreens() {
        return screens;
    }

    public void addScreen(FragmentInfo fragmentInfo) {
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
            String allParams = fragmentInfo.generateCommaSeparatedParams(true, paramInfo -> {
                String parameterName = paramInfo.getName();
                TypeName parameterType = paramInfo.getType();
                optionalScreenConstructor.addParameter(paramInfo.getParameterSpec());
                optionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                screenClass.addField(parameterType, parameterName, Modifier.PRIVATE);
            });
            screenClass.addMethod(optionalScreenConstructor.build());
            if (notOptionalScreenConstructor != null) {
                String notOptionalParams = fragmentInfo.generateCommaSeparatedParams(false, paramInfo -> {
                    String parameterName = paramInfo.getName();
                    if (!paramInfo.isOptional()) {
                        notOptionalScreenConstructor.addParameter(paramInfo.getParameterSpec());
                        notOptionalScreenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                    }
                });
                screenClass.addMethod(notOptionalScreenConstructor.build());
            }
            getFragmentMethod.addStatement("return $T.newInstance(" + allParams + ")", fragmentInfo.getMappedBindingType());
            screenClass.addMethod(getFragmentMethod.build());
            groupClass.addType(screenClass.build());
        });
        return groupClass.build();
    }
}