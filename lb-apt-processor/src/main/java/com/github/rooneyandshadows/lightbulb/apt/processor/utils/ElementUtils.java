package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.commons.MemberUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.*;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.PROTECTED;

@SuppressWarnings("unused")
public class ElementUtils {

    public static TypeName getTypeOfFieldElement(Element element) {
        return ClassName.get(element.asType());
    }

    public static boolean canBeInstantiated(Element classElement) {
        return classElement.getKind() == CLASS && !classElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    @NotNull
    public static TypeMirror getTypeMirror(Elements elements, Class<?> clazz) {
        return getTypeMirror(elements, clazz.getCanonicalName());
    }

    @NotNull
    public static TypeMirror getTypeMirror(Elements elements, String canonicalName) {
        TypeElement t = elements.getTypeElement(canonicalName);
        return t.asType();
    }

    @NotNull
    public static String getPackage(Elements elements, Element element) {
        return elements.getPackageOf(element)
                .getQualifiedName()
                .toString();
    }

    @NotNull
    public static String getPackage(Element element) {
        Element enclosing = element;

        while (enclosing.getKind() != PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }
        PackageElement packageElement = (PackageElement) enclosing;

        return packageElement.getQualifiedName().toString();
    }

    @Nullable
    public static TypeElement getSuperType(TypeElement element) {
        TypeMirror superClassTypeMirror = element.getSuperclass();

        if ((superClassTypeMirror instanceof NoType)) {
            return null;
        }

        return (TypeElement) ((DeclaredType) superClassTypeMirror).asElement();
    }

    @NotNull
    public static List<Element> getFieldElements(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(o -> o.getKind().isField())
                .collect(Collectors.toList());
    }

    @NotNull
    public static String getSimpleName(Element element) {
        return element.getSimpleName().toString();
    }

    @NotNull
    public static String getFullClassName(Elements elements, TypeElement element) {
        String classPackage = getPackage(elements, element);
        String classSimpleName = element.getSimpleName().toString();
        return classPackage.concat(".").concat(classSimpleName);
    }

    @Nullable
    public static String findFieldSetterName(TypeElement classElement, String fieldName) {
        String setterName = MemberUtils.getFieldSetterName(fieldName);
        boolean exists = methodExists(classElement, setterName);
        return exists ? setterName : null;
    }

    @Nullable
    public static String findFieldGetterName(TypeElement classElement, String fieldName) {
        String getterName = MemberUtils.getFieldGetterName(fieldName);
        boolean exists = methodExists(classElement, getterName);
        return exists ? getterName : null;
    }

    public static List<ExecutableElement> getMethods(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(target -> target.getKind() == METHOD)
                .map(element -> (ExecutableElement) element)
                .toList();
    }

    public static List<VariableElement> getFields(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(target -> target.getKind() == FIELD)
                .map(element -> (VariableElement) element)
                .toList();
    }

    private static boolean methodExists(Element classElement, String methodName) {
        if (classElement.getKind() != CLASS) return false;
        Pattern pattern = compile("^".concat(methodName).concat("(\\$.*)?$"), CASE_INSENSITIVE);
        return !classElement.getEnclosedElements().stream()
                .filter(target -> {
                    if (target.getKind() != METHOD) {
                        return false;
                    }
                    String targetName = target.getSimpleName().toString();
                    Matcher matcher = pattern.matcher(targetName);
                    return matcher.find();
                })
                .toList()
                .isEmpty();
    }

    @Nullable
    public static Modifier getAccessModifier(Element element) {
        return element.getModifiers()
                .stream()
                .filter(modifier -> {
                    return modifier == Modifier.PUBLIC || modifier == PROTECTED || modifier == Modifier.PRIVATE;
                }).findFirst()
                .orElse(null);
    }

    @NotNull
    public static Modifier accessModifierAtLeast(@Nullable Modifier accessModifier, @NotNull Modifier target) {

        if (accessModifier == null || target.ordinal() <= accessModifier.ordinal()) {
            return target;
        }

        return accessModifier;
    }

    public static boolean isAccessModifierAtLeastProtected(@NotNull Element test) {
        Modifier accessModifier = getAccessModifier(test);
        return accessModifier != null && PROTECTED.ordinal() >= accessModifier.ordinal();
    }

    @Nullable
    public static Modifier getMethodAccessModifier(Element classElement, String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return null;
        }

        Element method = classElement.getEnclosedElements()
                .stream()
                .filter(target -> {
                    String targetName = target.getSimpleName().toString();
                    return target.getKind() == METHOD && targetName.equals(methodName);
                }).findFirst()
                .orElse(null);

        if (method == null) {
            return null;
        }

        return getAccessModifier(method);
    }

    public static boolean isNullable(Element element) {
        return element.getAnnotation(Nullable.class) != null || element.getAnnotation(NotNull.class) == null;
    }

    public static boolean isFinal(Element element) {
        return element.getModifiers()
                .stream()
                .anyMatch(modifier -> modifier == Modifier.FINAL);
    }

    public static boolean hasAnnotation(AnnotatedConstruct element, Class<? extends Annotation> annotationTarget) {
        return element.getAnnotation(annotationTarget) != null;
    }
}