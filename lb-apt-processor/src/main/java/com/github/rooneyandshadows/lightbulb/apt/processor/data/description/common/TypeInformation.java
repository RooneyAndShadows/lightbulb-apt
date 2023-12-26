package com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;


@SuppressWarnings({"DuplicatedCode", "RedundantIfStatement", "unused"})
public class TypeInformation {
    protected final TypeMirror typeMirror;
    protected final TypeName typeName;
    protected final boolean isPrimitive;

    public TypeInformation(Element element) {
        this.typeMirror = element.asType();
        this.isPrimitive = !(typeMirror instanceof DeclaredType);
        this.typeName = ClassName.get(typeMirror);
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

    public <T> boolean is(@NotNull T type) {
        return is(type.toString(), typeMirror);
    }

    public <T> boolean is(@NotNull Class<T> clazz) {
        return is(clazz.getCanonicalName(), typeMirror);
    }

    public boolean is(@NotNull TypeName typeName) {
        return is(typeName.toString(), typeMirror);
    }

    public boolean is(@NotNull ClassName className) {
        return is(className.canonicalName(), typeMirror);
    }

    public boolean is(@NotNull String typeName) {
        return is(typeName, typeMirror);
    }

    private boolean is(@NotNull String typeName, @NotNull TypeMirror typeMirror) {
        if (isPrimitive) {
            return typeMirror.toString().equals(typeName);
        }

        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();

        if (typeName.equals(typeElement.getQualifiedName().toString())) {
            return true;
        }

        for (TypeMirror implementedInterface : typeElement.getInterfaces()) {
            if (is(typeName, implementedInterface)) {
                return true;
            }
        }

        TypeMirror superClassTypeMirror = typeElement.getSuperclass();
        if (!(superClassTypeMirror instanceof NoType)) {
            if (is(typeName, superClassTypeMirror)) {
                return true;
            }
        }

        return false;
    }

}