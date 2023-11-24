package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementUtils {

    public static TypeName getTypeOfFieldElement(Element element) {
        return ClassName.get(element.asType());
    }

    public static boolean canBeInstantiated(Element classElement) {
        return classElement.getKind() == ElementKind.CLASS && !classElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    public static String getPackage(Elements elements, Element element) {
        return elements.getPackageOf(element)
                .getQualifiedName()
                .toString();
    }

    public static TypeElement getSuperType(TypeElement element) {
        TypeMirror superClassTypeMirror = element.getSuperclass();
        return (TypeElement) ((DeclaredType) superClassTypeMirror).asElement();
    }

    public static String getSimpleName(Element element) {
        return element.getSimpleName().toString();
    }

    public static String getFullClassName(Elements elements, Element element) {
        String classPackage = getPackage(elements, element);
        String classSimpleName = element.getSimpleName().toString();
        return classPackage.concat(".").concat(classSimpleName);
    }

    public static String scanForSetter(Element classElement, String fieldName) {
        String setterName = "set".concat(capitalizeFirstLetter(fieldName));
        return methodExists(classElement, setterName);
    }

    public static String scanForGetter(Element classElement, String fieldName) {
        String getterName = "get".concat(capitalizeFirstLetter(fieldName));
        return methodExists(classElement, getterName);
    }

    private static String methodExists(Element classElement, String methodName) {
        if (classElement.getKind() != ElementKind.CLASS) return null;
        Pattern pattern = Pattern.compile("^".concat(methodName).concat("(\\$.*)?$"), Pattern.CASE_INSENSITIVE);
        return classElement.getEnclosedElements().stream()
                .filter(target -> {
                    String targetName = target.getSimpleName().toString();
                    boolean take = target.getKind() == ElementKind.METHOD;
                    Matcher matcher = pattern.matcher(targetName);
                    take &= matcher.find();
                    return take;
                })
                .map(element -> element.getSimpleName().toString())
                .findFirst()
                .orElse(null);
    }

    public static Modifier getAccessModifier(Element element) {
        return element.getModifiers()
                .stream()
                .filter(modifier -> {
                    return modifier == Modifier.PUBLIC || modifier == Modifier.PROTECTED || modifier == Modifier.PRIVATE;
                }).findFirst()
                .orElse(null);
    }

    public static Modifier getMethodAccessModifier(Element classElement, String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return null;
        }

        Element method = classElement.getEnclosedElements()
                .stream()
                .filter(target -> {
                    String targetName = target.getSimpleName().toString();
                    return target.getKind() == ElementKind.METHOD && targetName.equals(methodName);
                }).findFirst()
                .orElse(null);

        if (method == null) {
            return null;
        }

        return getAccessModifier(method);
    }

    public static boolean isFinal(Element element) {
        return element.getModifiers()
                .stream()
                .anyMatch(modifier -> modifier == Modifier.FINAL);
    }

    private static String capitalizeFirstLetter(String target) {
        return target.substring(0, 1).toUpperCase() + target.substring(1);
    }
}