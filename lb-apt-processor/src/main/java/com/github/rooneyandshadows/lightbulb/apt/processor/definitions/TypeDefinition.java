package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.BaseDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;


@SuppressWarnings({"DuplicatedCode", "RedundantIfStatement", "unused", "FieldCanBeLocal"})
public final class TypeDefinition extends BaseDefinition<TypeMirror> {
    private final TypeMirror typeMirror;
    private final boolean isPrimitive;
    private final boolean isNested;
    private final String packageName;
    private final String simpleResolvedName;
    private final String qualifiedResolvedName;

    public TypeDefinition(TypeMirror typeMirror) {
        super(typeMirror);
        this.typeMirror = typeMirror;
        this.isPrimitive = !(typeMirror instanceof DeclaredType);
        this.isNested = !isPrimitive && ((TypeElement) ((DeclaredType) typeMirror).asElement()).getNestingKind().isNested();
        this.packageName = extractPackageName();
        this.simpleResolvedName = extractResolvedSimpleName();
        this.qualifiedResolvedName = extractResolvedQualifiedName();
    }

    @Nullable
    public TypeDefinition getSuperClassType() {
        if (isPrimitive) {
            return null;
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
        TypeMirror superClassTypeMirror = typeElement.getSuperclass();

        if ((superClassTypeMirror instanceof NoType)) {
            return null;
        }

        TypeElement superclassTypeElement = (TypeElement) ((DeclaredType) superClassTypeMirror).asElement();

        return new TypeDefinition(superclassTypeElement.asType());
    }

    public boolean hasConstructorWithParameters(String... paramTypes) {
        return hasConstructorWithParameters(null, paramTypes);
    }

    public boolean hasConstructorWithParameters(Predicate<ExecutableElement> predicate, String... paramTypes) {
        if (isPrimitive) {
            return false;
        }
        DeclaredType declaredType = ((DeclaredType) typeMirror);
        TypeElement target = (TypeElement) declaredType.asElement();

        return target.getEnclosedElements()
                .stream()
                .anyMatch(element -> {
                    if (element.getKind() != CONSTRUCTOR) {
                        return false;
                    }

                    ExecutableElement constructor = (ExecutableElement) element;

                    if (constructor.getParameters().size() != paramTypes.length) {
                        return false;
                    }

                    for (int pos = 0; pos < constructor.getParameters().size(); pos++) {
                        VariableElement param = constructor.getParameters().get(pos);
                        String paramType = param.asType().toString();
                        if (!paramType.equals(paramTypes[pos])) {
                            return false;
                        }
                    }
                    return predicate == null || predicate.test((constructor));
                });
    }

    public boolean canBeInstantiated() {
        if (isPrimitive) {
            return false;
        } else {
            DeclaredType declaredType = ((DeclaredType) typeMirror);
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getKind() == ElementKind.CLASS && !typeElement.getModifiers().contains(Modifier.ABSTRACT);
        }
    }

    public List<TypeDefinition> getParametrizedTypes() {
        List<TypeDefinition> result = new ArrayList<>();

        if (!isParametrized()) {
            return result;
        }

        DeclaredType type = (DeclaredType) typeMirror;

        type.getTypeArguments().forEach(typeArg -> {
            TypeElement typeArgElement = (TypeElement) ((DeclaredType) typeArg).asElement();
            result.add(new TypeDefinition(typeArgElement.asType()));
        });

        return result;
    }


    public boolean isNested() {
        return isNested;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleName() {
        if (isPrimitive || !isNested) {
            return simpleResolvedName;
        }

        return ((DeclaredType) typeMirror).asElement().getSimpleName().toString();
    }

    public String getSimpleResolvedName() {
        return simpleResolvedName;
    }

    public String getQualifiedResolvedName() {
        return qualifiedResolvedName;
    }

    public String getQualifiedName() {
        if (isPrimitive || !isNested) {
            return qualifiedResolvedName;
        }

        return ((TypeElement) ((DeclaredType) typeMirror).asElement()).getQualifiedName().toString();
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
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
        return is(String.class.getCanonicalName());
    }

    public boolean isUUID() {
        return is(UUID.class.getCanonicalName());
    }

    public boolean isDate() {
        return is(Date.class.getCanonicalName());
    }

    public boolean isOffsetDate() {
        return is(OffsetDateTime.class.getCanonicalName());
    }

    public boolean isInt() {
        return isOneOf(Integer.class.getCanonicalName(), int.class.getCanonicalName());
    }

    public boolean isBoolean() {
        return isOneOf(Boolean.class.getCanonicalName(), boolean.class.getCanonicalName());
    }

    public boolean isFloat() {
        return isOneOf(Float.class.getCanonicalName(), float.class.getCanonicalName());
    }

    public boolean isLong() {
        return isOneOf(Long.class.getCanonicalName(), long.class.getCanonicalName());
    }

    public boolean isDouble() {
        return isOneOf(Double.class.getCanonicalName(), double.class.getCanonicalName());
    }

    public boolean isList() {
        return is(List.class.getCanonicalName());
    }

    public boolean isMap() {
        return is(Map.class.getCanonicalName());
    }

    public boolean is(@NotNull ClassDefinitions.ClassInfo classInfo) {
        return is(typeMirror, classInfo.getCannonicalName());
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

    private String extractPackageName() {
        String fullName = typeMirror.toString();

        if (isPrimitive) {
            int endIndex = fullName.lastIndexOf(".");
            return endIndex != -1 ? fullName.substring(0, endIndex - 1) : "";
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();

        Element enclosing = typeElement.getEnclosingElement();

        while (enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }

        return enclosing.toString();
    }

    private String extractResolvedSimpleName() {
        String simpleName = typeMirror.toString().replace(packageName.concat("."), "");

        return isNested ? simpleName.replace(".", "$") : simpleName;
    }

    private String extractResolvedQualifiedName() {
        String fullName = typeMirror.toString();

        if (isPrimitive || !isNested) {
            return fullName;
        }

        return packageName.concat(".").concat(simpleResolvedName);
    }
}