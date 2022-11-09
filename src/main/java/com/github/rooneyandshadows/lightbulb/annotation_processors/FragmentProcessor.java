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
            methods.add(generateFragmentCreatorMethod(classInfo));
            methods.add(generateFragmentConfigurationMethod(classInfo));
            methods.add(generateFragmentViewBindingsMethod(classInfo));
            if (classInfo.canBeInstantiated)
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
                classInfo.canBeInstantiated = canBeInstantiated(classElement);
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

    private boolean canBeInstantiated(Element classElement) {
        return !classElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    private boolean obtainAnnotatedClassesWithFragmentConfiguration(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FragmentConfiguration.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@FragmentConfiguration should be on top of fragment classes.");
                return false;
            }
            ClassInfo classInfo = new ClassInfo();
            classInfo.canBeInstantiated = canBeInstantiated(element);
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
                classInfo.canBeInstantiated = canBeInstantiated(classElement);
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
        String bundleType = ClassName.get("android.os", "Bundle").toString();
        fragmentHandleParametersMethod.addStatement(bundleType + " arguments = fragment.getArguments()");
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            TypeName type = fragmentParameterInfo.type;
            String name = fragmentParameterInfo.name;
            boolean optional = fragmentParameterInfo.optional;
            fragmentHandleParametersMethod.addStatement(type + " " + name + " = " + resolveParameterExpression(type, name, "get"));
            if (!optional) {
                fragmentHandleParametersMethod.beginControlFlow("if(" + name + " == null)")
                        .addStatement("throw new java.lang.IllegalArgumentException(\"Argument " + name + " is not optional.\")")
                        .endControlFlow();
            }
            fragmentHandleParametersMethod.addStatement("fragment.$L = $L", name, name);
        });
        return fragmentHandleParametersMethod.build();
    }

    private MethodSpec generateFragmentCreatorMethod(ClassInfo classInfo) {
        TypeName fragmentType = TypeName.get(classInfo.type);
        ClassName bundleType = ClassName.get("android.os", "Bundle");
        MethodSpec.Builder fragmentHandleParametersMethod = MethodSpec
                .methodBuilder("newInstance");
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            fragmentHandleParametersMethod.addParameter(fragmentParameterInfo.type, fragmentParameterInfo.name);
        });
        fragmentHandleParametersMethod
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentType)
                .addStatement("$T  fragment = new $T()", fragmentType, fragmentType)
                .addStatement("$T  arguments = new $T()", bundleType, bundleType);
        classInfo.fragmentParameters.forEach(fragmentParameterInfo -> {
            fragmentHandleParametersMethod.addStatement(resolveParameterExpression(fragmentParameterInfo.type, fragmentParameterInfo.name, "put"));
        });
        fragmentHandleParametersMethod.addStatement("fragment.setArguments(arguments)");
        fragmentHandleParametersMethod.addStatement("return fragment");
        return fragmentHandleParametersMethod.build();
    }

    private String resolveParameterExpression(TypeName type, String parameterName, String bundleAction) {
        String typeString = type.toString();
        if (typeString.equals(stringType)) {
            return String.format("arguments.%sString(\"" + parameterName + "\")", bundleAction);
        } else if (typeString.equals(uuidType)) {
            return String.format(uuidType + ".fromString(arguments.%sString(\"" + parameterName + "\"))", bundleAction);
        } else if (typeString.equals(intType) || typeString.equals(intPrimType)) {
            return String.format("arguments.%sInt(\"" + parameterName + "\")", bundleAction);
        } else if (typeString.equals(booleanType) || typeString.equals(booleanPrimType)) {
            return String.format("arguments.%sBoolean(\"" + parameterName + "\")", bundleAction);
        } else if (typeString.equals(floatType) || typeString.equals(floatPrimType)) {
            return String.format("arguments.%sFloat(\"" + parameterName + "\")", bundleAction);
        } else if (typeString.equals(longType) || typeString.equals(longPrimType)) {
            return String.format("arguments.%sLong(\"" + parameterName + "\")", bundleAction);
        } else if (typeString.equals(doubleType) || typeString.equals(doublePrimType)) {
            return String.format("arguments.%sDouble(\"" + parameterName + "\")", bundleAction);
        } else {
            return String.format("arguments.%sParcelable(\"" + parameterName + "\")", bundleAction);
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

    private TypeName getTypeOfFieldElement(Element element) {
        return ClassName.get(element.asType());
    }

    private static class ClassInfo {
        private TypeMirror type;
        private String classPackage;
        private String simpleClassName;
        private String fullClassName;
        private boolean canBeInstantiated;
        private FragmentConfiguration configAnnotation;
        private final Map<String, String> viewBindings = new HashMap<>();
        private final List<FragmentParameterInfo> fragmentParameters = new ArrayList<>();
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