package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
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

@AutoService(Processor.class)
public class FragmentProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;
    private List<ClassInfo> classInfoList;
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
        this.classInfoList = new ArrayList<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //Get annotated targets
        boolean processResult;
        processResult = obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment);
        processResult &= obtainAnnotatedFieldsWithBindView(roundEnvironment);
        processResult &= obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment);
        if (!processResult) {
            return false;
        }
        //Generate methods
        List<MethodSpec> methods = new ArrayList<>();
        classInfoList.forEach(classInfo -> {
            methods.clear();
            methods.add(generateFragmentConfigurationMethod(classInfo));
            methods.add(generateFragmentViewBindingsMethod(classInfo));
            methods.add(generateFragmentParametersMethod(classInfo));
            TypeSpec.Builder generatedClass = TypeSpec
                    .classBuilder(classInfo.simpleClassName.concat("Bindings"))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methods);
            try {
                JavaFile.builder(classInfo.classPackage, generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(BindView.class.getCanonicalName());
                add(FragmentConfiguration.class.getCanonicalName());
                add(FragmentParameter.class.getCanonicalName());
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private boolean obtainAnnotatedFieldsWithFragmentParameter(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentParameter.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.");
                return false;
            }
            FragmentParameter annotation = element.getAnnotation(FragmentParameter.class);
            Element classElement = element.getEnclosingElement();
            String classPackage = getPackage(elements, element);
            String fullClassName = getFullClassName(elements, classElement);
            ClassInfo classInfo = classInfoList.stream().filter(info -> info.fullClassName.equals(fullClassName))
                    .findFirst()
                    .orElse(null);
            if (classInfo == null) {
                classInfo = new ClassInfo();
                classInfo.classPackage = classPackage;
                classInfo.type = classElement.asType();
                classInfo.simpleClassName = classElement.getSimpleName().toString();
                classInfoList.add(classInfo);
            }
            FragmentParameterInfo info = new FragmentParameterInfo(element.getSimpleName().toString(), getTypeOfFieldElement(element), annotation.optional());
            classInfo.fragmentParameters.add(info);
        }
        return true;
    }

    private boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            ClassInfo classInfo = new ClassInfo();
            classInfo.type = element.asType();
            classInfo.simpleClassName = element.getSimpleName().toString();
            classInfo.classPackage = getPackage(elements, element);
            classInfo.fullClassName = getFullClassName(elements, element);
            classInfo.configAnnotation = element.getAnnotation(FragmentConfiguration.class);
            classInfoList.add(classInfo);
        }
        return true;
    }

    private boolean obtainAnnotatedFieldsWithBindView(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@BindView should be on top of fragment field.");
                return false;
            }
            BindView annotation = element.getAnnotation(BindView.class);
            Element classElement = element.getEnclosingElement();
            String classPackage = getPackage(elements, element);
            String fullClassName = getFullClassName(elements, classElement);
            ClassInfo classInfo = classInfoList.stream().filter(info -> info.fullClassName.equals(fullClassName))
                    .findFirst()
                    .orElse(null);
            if (classInfo == null) {
                classInfo = new ClassInfo();
                classInfo.classPackage = classPackage;
                classInfo.type = classElement.asType();
                classInfo.simpleClassName = classElement.getSimpleName().toString();
                classInfoList.add(classInfo);
            }
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
        MethodSpec.Builder fragViewBindingMethod = MethodSpec
                .methodBuilder("generate" + classInfo.simpleClassName + "ViewBindings")
                .addParameter(TypeName.get(classInfo.type), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        classInfo.viewBindings.forEach((fieldName, identifierName) -> {
            fragViewBindingMethod.addStatement("fragment.$L = fragment.getView().findViewById(fragment.getResources().getIdentifier($S, \"id\", fragment.getActivity().getPackageName()))", fieldName, identifierName);
        });
        return fragViewBindingMethod.build();
    }

    private MethodSpec generateFragmentParametersMethod(ClassInfo classInfo) {
        MethodSpec.Builder fragmentHandleParametersMethod = MethodSpec
                .methodBuilder("generate" + classInfo.simpleClassName + "Parameters")
                .addParameter(TypeName.get(classInfo.type), "fragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            String type = fragmentParameterInfo.type;
            String name = fragmentParameterInfo.name;
            boolean optional = fragmentParameterInfo.optional;
            String bundleType = ClassName.get("android.os", "Bundle").toString();
            fragmentHandleParametersMethod.addStatement(bundleType + " arguments = fragment.getArguments()");
            fragmentHandleParametersMethod.addStatement(type + " " + name + " = " + resolveParameterExpression(type, name));
            if (optional)
                fragmentHandleParametersMethod.addStatement("if(" + name + " == null) throw new java.lang.IllegalArgumentException(\"Argument is not optional. You must specify value.\")");
            fragmentHandleParametersMethod.addStatement("fragment.$L = " + resolveParameterExpression(type, name), name);
        });
        return fragmentHandleParametersMethod.build();
    }

    private String resolveParameterExpression(String type, String parameterName) {
        if (type.equals(stringType)) {
            return "arguments.getString(\"" + parameterName + "\")";
        } else if (type.equals(uuidType)) {
            return uuidType + ".fromString(arguments.getString(\"" + parameterName + "\"))";
        } else if (type.equals(intType) || type.equals(intPrimType)) {
            return "arguments.getInt(\"" + parameterName + "\")";
        } else if (type.equals(booleanType) || type.equals(booleanPrimType)) {
            return "arguments.getBoolean(\"" + parameterName + "\")";
        } else if (type.equals(floatType) || type.equals(floatPrimType)) {
            return "arguments.getFloat(\"" + parameterName + "\")";
        } else if (type.equals(longType) || type.equals(longPrimType)) {
            return "arguments.getLong(\"" + parameterName + "\")";
        } else if (type.equals(doubleType) || type.equals(doublePrimType)) {
            return "arguments.getDouble(\"" + parameterName + "\")";
        } else {
            return "arguments.getParcelable(\"" + parameterName + "\")";
        }
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

    private String getTypeOfFieldElement(Element element) {
        TypeName className = ClassName.get(element.asType());
        String name = className.toString();
        return name;
    }

    private static class ClassInfo {
        private TypeMirror type;
        private String classPackage;
        private String simpleClassName;
        private String fullClassName;
        private FragmentConfiguration configAnnotation;
        private final Map<String, String> viewBindings = new HashMap<>();
        private final List<FragmentParameterInfo> fragmentParameters = new ArrayList<>();
    }

    private static class FragmentParameterInfo {
        private final String name;
        private final String type;
        private final boolean optional;

        public FragmentParameterInfo(String name, String type, boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
        }
    }
}