package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.*;


@SuppressWarnings({"DuplicatedCode", "RedundantIfStatement", "unused"})
public class TypeInformation {
    private static final String stringType = String.class.getCanonicalName();
    private static final String intType = Integer.class.getCanonicalName();
    private static final String intPrimType = int.class.getCanonicalName();
    private static final String booleanType = Boolean.class.getCanonicalName();
    private static final String booleanPrimType = boolean.class.getCanonicalName();
    private static final String uuidType = java.util.UUID.class.getCanonicalName();
    private static final String floatType = Float.class.getCanonicalName();
    private static final String floatPrimType = float.class.getCanonicalName();
    private static final String longType = Long.class.getCanonicalName();
    private static final String longPrimType = long.class.getCanonicalName();
    private static final String doubleType = Double.class.getCanonicalName();
    private static final String doublePrimType = double.class.getCanonicalName();
    private static final String dateType = Date.class.getCanonicalName();
    private static final String offsetDateType = OffsetDateTime.class.getCanonicalName();
    private static final String listType = List.class.getCanonicalName();
    private static final String mapType = Map.class.getCanonicalName();
    protected final TypeMirror typeMirror;
    protected final TypeName typeName;
    protected final boolean isPrimitive;

    public TypeInformation(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
        this.isPrimitive = !(typeMirror instanceof DeclaredType);
        this.typeName = ClassName.get(typeMirror);
    }

    public boolean canBeInstantiated(){
        if (isPrimitive) {
            return false;
        } else {
            DeclaredType declaredType = ((DeclaredType) typeMirror);
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getKind() == ElementKind.CLASS && !typeElement.getModifiers().contains(Modifier.ABSTRACT);
        }
    }

    public List<TypeInformation> getParametrizedTypes() {
        List<TypeInformation> result = new ArrayList<>();

        if (!isParametrized()) {
            return result;
        }

        DeclaredType type = (DeclaredType) typeMirror;

        type.getTypeArguments().forEach(typeArg -> {
            result.add(new TypeInformation(typeArg));
        });

        return result;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isParametrized() {
        if (isPrimitive) {
            return false;
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();

        return !typeElement.getTypeParameters().isEmpty();
    }

    public boolean isString() {
        return is(stringType);
    }

    public boolean isUUID() {
        return is(uuidType);
    }

    public boolean isDate() {
        return is(dateType);
    }

    public boolean isOffsetDate() {
        return is(offsetDateType);
    }

    public boolean isInt() {
        return isOneOf(intType, intPrimType);
    }

    public boolean isBoolean() {
        return isOneOf(booleanType, booleanPrimType);
    }

    public boolean isFloat() {
        return isOneOf(floatType, floatPrimType);
    }

    public boolean isLong() {
        return isOneOf(longType, longPrimType);
    }

    public boolean isDouble() {
        return isOneOf(doubleType, doublePrimType);
    }

    public boolean isList() {
        return is(listType);
    }

    public boolean isMap() {
        return is(mapType);
    }

    public boolean is(@NotNull Type type) {
        String target = type.getTypeName();
        return is(typeMirror, target);
    }

    public boolean is(@NotNull Class<?> classTarget) {
        String target = classTarget.getCanonicalName();
        return is(typeMirror, target);
    }

    public boolean is(@NotNull TypeName typeName) {
        String target = typeName.toString();
        return is(typeMirror, target);
    }

    public boolean is(@NotNull ClassName className) {
        String target = className.canonicalName();
        return is(typeMirror, target);
    }

    public boolean is(@NotNull String typeName) {
        return is(typeMirror, typeName);
    }

    public boolean isOneOf(@NotNull Type... types) {
        String[] targets = Arrays.stream(types)
                .map(Type::toString)
                .toArray(String[]::new);
        return isOneOf(typeMirror, targets);
    }

    public boolean isOneOf(@NotNull Class<?>... classTargets) {
        String[] targets = Arrays.stream(classTargets)
                .map(Class::getCanonicalName)
                .toArray(String[]::new);
        return isOneOf(typeMirror, targets);
    }

    public boolean isOneOf(@NotNull TypeName... typeNames) {
        String[] targets = Arrays.stream(typeNames)
                .map(TypeName::toString)
                .toArray(String[]::new);
        return isOneOf(typeMirror, targets);
    }

    public boolean isOneOf(@NotNull ClassName... classNames) {
        String[] typeNames = Arrays.stream(classNames)
                .map(ClassName::canonicalName)
                .toArray(String[]::new);
        return isOneOf(typeMirror, typeNames);
    }

    public boolean isOneOf(@NotNull String... typeNames) {
        return isOneOf(typeMirror, typeNames);
    }

    private boolean isOneOf(@NotNull TypeMirror typeMirror, @NotNull String... typeNames) {
        List<String> targetTypes = Arrays.asList(typeNames);

        if (isPrimitive) {
            String currentType = typeMirror.toString();
            return targetTypes.contains(currentType);
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
        String currentType = typeElement.getQualifiedName().toString();

        if (targetTypes.contains(currentType)) {
            return true;
        }

        for (TypeMirror implementedInterfaceTypeMirror : typeElement.getInterfaces()) {
            if (isOneOf(implementedInterfaceTypeMirror, typeNames)) {
                return true;
            }
        }

        TypeMirror superClassTypeMirror = typeElement.getSuperclass();

        if (!(superClassTypeMirror instanceof NoType)) {
            if (isOneOf(superClassTypeMirror, typeNames)) {
                return true;
            }
        }

        return false;
    }

    private boolean is(@NotNull TypeMirror typeMirror, @NotNull String typeName) {
        if (isPrimitive) {
            return typeMirror.toString().equals(typeName);
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();

        if (typeName.equals(typeElement.getQualifiedName().toString())) {
            return true;
        }

        for (TypeMirror implementedInterface : typeElement.getInterfaces()) {
            if (is(implementedInterface, typeName)) {
                return true;
            }
        }

        TypeMirror superClassTypeMirror = typeElement.getSuperclass();

        if (!(superClassTypeMirror instanceof NoType)) {
            if (is(superClassTypeMirror, typeName)) {
                return true;
            }
        }

        return false;
    }
}