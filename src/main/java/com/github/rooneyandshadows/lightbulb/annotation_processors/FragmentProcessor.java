package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SuppressWarnings("FieldCanBeLocal")
@AutoService(Processor.class)
public class FragmentProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;
    private final List<ClassInfo> classInfoList = new ArrayList<>();
    private final List<FragmentScreenGroup> screenGroups = new ArrayList<>();
    private final String stringType = String.class.getCanonicalName();
    private final String intType = Integer.class.getCanonicalName();
    private final String intPrimType = int.class.getCanonicalName();
    private final String booleanType = Boolean.class.getCanonicalName();
    private final String booleanPrimType = boolean.class.getCanonicalName();
    private final String uuidType = UUID.class.getCanonicalName();
    private final String floatType = Float.class.getCanonicalName();
    private final String floatPrimType = float.class.getCanonicalName();
    private final String longType = Long.class.getCanonicalName();
    private final String longPrimType = long.class.getCanonicalName();
    private final String doubleType = Double.class.getCanonicalName();
    private final String doublePrimType = double.class.getCanonicalName();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
        this.elements = processingEnvironment.getElementUtils();
        this.types = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //Get annotated targets
        boolean processResult;
        processResult = obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment);
        processResult &= obtainAnnotatedClassesWithFragmentScreen(roundEnvironment);
        processResult &= obtainAnnotatedFieldsWithBindView(roundEnvironment);
        processResult &= obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment);
        if (!processResult) {
            return false;
        }
        generateFragmentBindingClasses();
        generateRoutingScreens();
        generateRouter();
        //Generate methods
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(BindView.class.getCanonicalName());
                add(FragmentConfiguration.class.getCanonicalName());
                add(FragmentParameter.class.getCanonicalName());
                add(FragmentScreen.class.getCanonicalName());
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void generateFragmentBindingClasses() {
        List<MethodSpec> methods = new ArrayList<>();
        classInfoList.forEach(classInfo -> {
            methods.clear();
            String className = classInfo.simpleClassName.concat("Bindings");
            if (classInfo.canBeInstantiated)
                methods.add(generateFragmentCreatorMethod(classInfo));
            methods.add(generateFragmentConfigurationMethod(classInfo));
            methods.add(generateFragmentViewBindingsMethod(classInfo));
            methods.add(generateFragmentParametersMethod(classInfo));
            methods.add(generateSaveVariablesMethod(classInfo));
            methods.add(generateRestoreVariablesMethod(classInfo));
            TypeSpec.Builder generatedClass = TypeSpec
                    .classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methods);
            classInfo.mappedBindingType = ClassName.get(classInfo.classPackage, className);
            try {
                JavaFile.builder(classInfo.classPackage, generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private boolean obtainAnnotatedClassesWithFragmentScreen(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentScreen.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentScreen should be on top of fragment classes.");
                return false;
            }
            FragmentScreen annotation = classElement.getAnnotation(FragmentScreen.class);
            ClassInfo classInfo = getOrCreateClassInfoForElement(classElement);
            classInfo.screenName = annotation.screenName();
            CreateOrUpdateScreenGroup(classInfo, annotation.screenGroup());
        }
        return true;
    }


    private boolean obtainAnnotatedFieldsWithFragmentParameter(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentParameter.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentParameter should be on top of fragment field.");
                return false;
            }
            FragmentParameter annotation = element.getAnnotation(FragmentParameter.class);
            Element classElement = element.getEnclosingElement();
            ClassInfo classInfo = getOrCreateClassInfoForElement(classElement);
            FragmentParameterInfo info = new FragmentParameterInfo(element.getSimpleName().toString(), getTypeOfFieldElement(element), annotation.optional());
            classInfo.fragmentParameters.add(info);
        }
        return true;
    }

    private boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element classElement : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            ClassInfo classInfo = getOrCreateClassInfoForElement(classElement);
            classInfo.configAnnotation = classElement.getAnnotation(FragmentConfiguration.class);
        }
        return true;
    }

    private void generateRouter() {
        String routerPackage = "com.github.rooneyandshadows.lightbulb.routing";
        String screensPackage = "com.github.rooneyandshadows.lightbulb.routing.screens";
        ClassName baseRouterClass = ClassName.get("com.github.rooneyandshadows.lightbulb.application.activity.routing", "BaseActivityRouter");
        ClassName screensClass = ClassName.get(screensPackage, "Screens");
        ClassName baseActivityClass = ClassName.get("com.github.rooneyandshadows.lightbulb.application.activity", "BaseActivity");
        TypeSpec.Builder routerClass = TypeSpec
                .classBuilder("AppRouter")
                .superclass(baseRouterClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(baseActivityClass, "contextActivity")
                                .addParameter(TypeName.INT, "fragmentContainerId")
                                .addStatement("super(contextActivity,fragmentContainerId)")
                                .build()
                );
        screenGroups.forEach(group -> {
            String groupName = group.screenGroupName;
            group.screens.forEach(classInfo -> {
                ClassName groupClass = screensClass.nestedClass(groupName);
                ClassName screenClass = groupClass.nestedClass(classInfo.screenName);
                String methodName = "to" + classInfo.screenName + groupName;
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
                String paramsString = "";
                for (int i = 0; i < classInfo.fragmentParameters.size(); i++) {
                    boolean isLast = i == classInfo.fragmentParameters.size() - 1;
                    FragmentParameterInfo param = classInfo.fragmentParameters.get(i);
                    TypeName paramType = param.type;
                    String paramName = param.name;
                    methodBuilder.addParameter(paramType, paramName);
                    paramsString = paramsString.concat(isLast ? paramName : paramName.concat(", "));
                }
                methodBuilder.addStatement("forward(new $T($L))", screenClass, paramsString);
                routerClass.addMethod(methodBuilder.build());
            });
        });
        try {
            JavaFile.builder(routerPackage, routerClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void generateRoutingScreens() {
        String packageName = "com.github.rooneyandshadows.lightbulb.routing.screens";
        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder("Screens")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        screenGroups.forEach(group -> {
            rootClass.addType(group.build());
        });
        try {
            JavaFile.builder(packageName, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private boolean obtainAnnotatedFieldsWithBindView(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.");
                return false;
            }
            Element classElement = element.getEnclosingElement();
            ClassInfo classInfo = getOrCreateClassInfoForElement(classElement);
            BindView annotation = element.getAnnotation(BindView.class);
            classInfo.viewBindings.put(element.getSimpleName().toString(), annotation.name());
        }
        return true;
    }

    private MethodSpec generateFragmentConfigurationMethod(ClassInfo classInfo) {
        boolean hasFragmentConfigAnnotation = classInfo.configAnnotation != null;
        if (!hasFragmentConfigAnnotation)
            return null;
        String layoutName = classInfo.configAnnotation.layoutName();
        String isMainScreenFragment = String.valueOf(classInfo.configAnnotation.isMainScreenFragment());
        String hasLeftDrawer = String.valueOf(classInfo.configAnnotation.hasLeftDrawer());
        String hasOptionsMenu = String.valueOf(classInfo.configAnnotation.hasOptionsMenu());
        return MethodSpec
                .methodBuilder("generate" + classInfo.simpleClassName + "Configuration")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(FragmentConfig.class)
                .addStatement("return new $T($S,$L,$L,$L)", ClassName.get(FragmentConfig.class), layoutName, isMainScreenFragment, hasLeftDrawer, hasOptionsMenu)
                .build();
    }

    private MethodSpec generateFragmentViewBindingsMethod(ClassInfo classInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("generate" + classInfo.simpleClassName + "ViewBindings")
                .addParameter(TypeName.get(classInfo.type), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        classInfo.viewBindings.forEach((fieldName, identifierName) -> {
            method.addStatement("fragment.$L = fragment.getView().findViewById(fragment.getResources().getIdentifier($S, \"id\", fragment.getActivity().getPackageName()))", fieldName, identifierName);
        });
        return method.build();
    }

    private MethodSpec generateFragmentParametersMethod(ClassInfo classInfo) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("generate" + classInfo.simpleClassName + "Parameters")
                .addParameter(TypeName.get(classInfo.type), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        String bundleType = ClassName.get("android.os", "Bundle").toString();
        method.addStatement(bundleType + " arguments = fragment.getArguments()");
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            TypeName type = fragmentParameterInfo.type;
            String name = fragmentParameterInfo.name;
            boolean optional = fragmentParameterInfo.optional;
            method.addStatement(type + " " + name + " = " + resolveReadParamFromBundleExpression(type, name, "arguments"));
            if (!optional && !type.isPrimitive()) {
                method.beginControlFlow("if(" + name + " == null)")
                        .addStatement("throw new java.lang.IllegalArgumentException(\"Argument " + name + " is not optional.\")")
                        .endControlFlow();
            }
            method.addStatement("fragment.$L = $L", name, name);
        });
        return method.build();
    }

    private MethodSpec generateFragmentCreatorMethod(ClassInfo classInfo) {
        TypeName fragmentType = TypeName.get(classInfo.type);
        ClassName bundleType = ClassName.get("android.os", "Bundle");
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("newInstance");
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            method.addParameter(fragmentParameterInfo.type, fragmentParameterInfo.name);
        });
        method.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentType)
                .addStatement("$T  fragment = new $T()", fragmentType, fragmentType)
                .addStatement("$T  arguments = new $T()", bundleType, bundleType);
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            method.addStatement(resolveWriteParamInBundleExpression(fragmentParameterInfo.type, fragmentParameterInfo.name, fragmentParameterInfo.name, "arguments"));
        });
        method.addStatement("fragment.setArguments(arguments)");
        method.addStatement("return fragment");
        return method.build();
    }

    private MethodSpec generateSaveVariablesMethod(ClassInfo classInfo) {
        ClassName bundleType = ClassName.get("android.os", "Bundle");
        TypeName fragmentType = TypeName.get(classInfo.type);
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(bundleType, "outState")
                .addParameter(fragmentType, "fragment")
                .returns(void.class);
        classInfo.fragmentParameters.forEach(param -> {
            method.addStatement(resolveWriteParamInBundleExpression(param.type, param.name, "fragment.".concat(param.name), "outState"));
        });
        return method.build();
    }

    private MethodSpec generateRestoreVariablesMethod(ClassInfo classInfo) {
        ClassName bundleType = ClassName.get("android.os", "Bundle");
        TypeName fragmentType = TypeName.get(classInfo.type);
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(bundleType, "fragmentSavedInstanceState")
                .addParameter(fragmentType, "fragment")
                .returns(void.class);
        classInfo.fragmentParameters.forEach(param -> {
            method.addStatement("fragment.$L = $L", param.name, resolveReadParamFromBundleExpression(param.type, param.name, "fragmentSavedInstanceState"));
        });
        return method.build();
    }

    private String resolveReadParamFromBundleExpression(TypeName type, String parameterName, String bundleVariableName) {
        String typeString = type.toString();
        if (typeString.equals(stringType)) {
            return String.format("%s.getString(\"" + parameterName + "\")", bundleVariableName);
        } else if (typeString.equals(uuidType)) {
            return String.format(uuidType + ".fromString(%s.getString(\"" + parameterName + "\"))", bundleVariableName);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            return String.format("%s.getInt(\"" + parameterName + "\")", bundleVariableName);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            return String.format("%s.getBoolean(\"" + parameterName + "\")", bundleVariableName);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            return String.format("%s.getFloat(\"" + parameterName + "\")", bundleVariableName);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            return String.format("%s.getLong(\"" + parameterName + "\")", bundleVariableName);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            return String.format("%s.getDouble(\"" + parameterName + "\")", bundleVariableName);
        } else {
            return String.format("%s.getParcelable(\"" + parameterName + "\")", bundleVariableName);
        }
    }

    private String resolveWriteParamInBundleExpression(TypeName type, String parameterKey, String parameterName, String bundleVariableName) {
        String typeString = type.toString();
        if (typeString.equals(stringType)) {
            return String.format("%s.putString(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else if (typeString.equals(uuidType)) {
            return String.format("%s.putString(\"" + parameterKey + "\", " + parameterName + ".toString())", bundleVariableName);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            return String.format("%s.putInt(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            return String.format("%s.putBoolean(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            return String.format("%s.putFloat(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            return String.format("%s.putLong(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            return String.format("%s.putDouble(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        } else {
            return String.format("%s.putParcelable(\"" + parameterKey + "\", " + parameterName + ")", bundleVariableName);
        }
    }

    private void CreateOrUpdateScreenGroup(ClassInfo classInfo, String screenGroup) {
        FragmentScreenGroup group = screenGroups.stream().filter(info -> info.screenGroupName.equals(screenGroup))
                .findFirst()
                .orElse(null);
        if (group == null) {
            group = new FragmentScreenGroup(screenGroup);
            screenGroups.add(group);
        }
        group.addScreen(classInfo);
    }

    private ClassInfo getOrCreateClassInfoForElement(Element classElement) {
        String classPackage = getPackage(elements, classElement);
        String fullClassName = getFullClassName(elements, classElement);
        ClassInfo existingClassInfo = classInfoList.stream().filter(info -> info.fullClassName.equals(fullClassName))
                .findFirst()
                .orElse(null);
        ClassInfo classInfo;
        if (existingClassInfo == null) {
            classInfo = new ClassInfo();
            classInfo.canBeInstantiated = canBeInstantiated(classElement);
            classInfo.type = classElement.asType();
            classInfo.simpleClassName = classElement.getSimpleName().toString();
            classInfo.classPackage = classPackage;
            classInfo.fullClassName = fullClassName;
            classInfoList.add(classInfo);
        } else {
            classInfo = existingClassInfo;
        }
        return classInfo;
    }

    private String getPackage(Elements elements, Element element) {
        return elements.getPackageOf(element)
                .getQualifiedName()
                .toString();
    }

    private String getFullClassName(Elements elements, Element element) {
        String classPackage = getPackage(elements, element);
        String classSimpleName = element.getSimpleName().toString();
        return classPackage.concat(".").concat(classSimpleName);
    }

    private TypeName getTypeOfFieldElement(Element element) {
        return ClassName.get(element.asType());
    }

    private boolean canBeInstantiated(Element classElement) {
        return !classElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    private static class ClassInfo {
        private TypeMirror type;
        private String classPackage;
        private String simpleClassName;
        private String fullClassName;
        private String screenName;
        private ClassName mappedBindingType;
        private boolean canBeInstantiated;
        private FragmentConfiguration configAnnotation;
        private final Map<String, String> viewBindings = new HashMap<>();
        private final List<FragmentParameterInfo> fragmentParameters = new ArrayList<>();
    }

    private static class FragmentScreenGroup {
        private final String screenGroupName;
        private final ArrayList<ClassInfo> screens = new ArrayList<>();

        public FragmentScreenGroup(String screenGroupName) {
            if (screenGroupName == null || screenGroupName.equals(""))
                screenGroupName = "Common";
            this.screenGroupName = screenGroupName;
        }

        private void addScreen(ClassInfo classInfo) {
            screens.add(classInfo);
        }

        private TypeSpec build() {
            ClassName baseRouterClass = ClassName.get("com.github.rooneyandshadows.lightbulb.application.activity.routing", "BaseActivityRouter");
            ClassName fragmentScreenClass = baseRouterClass.nestedClass("FragmentScreen");
            TypeSpec.Builder groupClass = TypeSpec
                    .classBuilder(screenGroupName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            screens.forEach(classInfo -> {
                TypeName currentFragmentClass = ClassName.get(classInfo.type);
                MethodSpec.Builder screenConstructor = MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC);
                TypeSpec.Builder screenClass = TypeSpec
                        .classBuilder(classInfo.screenName)
                        .superclass(fragmentScreenClass)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
                MethodSpec.Builder getFragmentMethod = MethodSpec
                        .methodBuilder("getFragment")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(currentFragmentClass);
                String paramsString = "";
                for (int i = 0; i < classInfo.fragmentParameters.size(); i++) {
                    boolean isLast = i == classInfo.fragmentParameters.size() - 1;
                    FragmentParameterInfo paramInfo = classInfo.fragmentParameters.get(i);
                    TypeName parameterType = paramInfo.type;
                    String parameterName = paramInfo.name;
                    screenConstructor.addParameter(parameterType, parameterName);
                    screenConstructor.addStatement("this.$L = $L", parameterName, parameterName);
                    screenClass.addField(parameterType, parameterName, Modifier.PRIVATE);
                    paramsString = paramsString.concat(isLast ? parameterName : parameterName.concat(", "));
                }

                screenClass.addMethod(screenConstructor.build());
                getFragmentMethod.addStatement("return $T.newInstance(" + paramsString + ")", classInfo.mappedBindingType);
                screenClass.addMethod(getFragmentMethod.build());
                groupClass.addType(screenClass.build());
            });
            return groupClass.build();
        }
    }

    private static class FragmentParameterInfo {
        private final String name;
        private final TypeName type;
        private final boolean optional;

        public FragmentParameterInfo(String name, TypeName type, boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
        }
    }
}