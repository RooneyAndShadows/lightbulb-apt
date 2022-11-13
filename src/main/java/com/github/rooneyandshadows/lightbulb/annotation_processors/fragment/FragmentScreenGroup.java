package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;

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
            MethodSpec.Builder screenConstructor = MethodSpec
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
            String paramsString = "";
            for (int i = 0; i < fragmentInfo.getFragmentParameters().size(); i++) {
                boolean isLast = i == fragmentInfo.getFragmentParameters().size() - 1;
                FragmentParamInfo paramInfo = fragmentInfo.getFragmentParameters().get(i);
                TypeName parameterType = paramInfo.getType();
                String parameterName = paramInfo.getName();
                screenConstructor.addParameter(parameterType, parameterName);
                screenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                screenClass.addField(parameterType, parameterName, Modifier.PRIVATE);
                paramsString = paramsString.concat(isLast ? parameterName : parameterName.concat(", "));
            }
            screenClass.addMethod(screenConstructor.build());
            getFragmentMethod.addStatement("return $T.newInstance(" + paramsString + ")", fragmentInfo.getMappedBindingType());
            screenClass.addMethod(getFragmentMethod.build());
            groupClass.addType(screenClass.build());
        });
        return groupClass.build();
    }
}