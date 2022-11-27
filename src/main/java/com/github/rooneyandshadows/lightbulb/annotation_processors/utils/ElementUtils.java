package com.github.rooneyandshadows.lightbulb.annotation_processors.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

public class ElementUtils {
    public static String getPackage(Elements elements, Element element) {
        return elements.getPackageOf(element)
                .getQualifiedName()
                .toString();
    }

    public static String getFullClassName(Elements elements, Element element) {
        String classPackage = getPackage(elements, element);
        String classSimpleName = element.getSimpleName().toString();
        return classPackage.concat(".").concat(classSimpleName);
    }

    public static TypeName getTypeOfFieldElement(Element element) {
        return ClassName.get(element.asType());
    }

    public static boolean canBeInstantiated(Element classElement) {
        return classElement.getKind() == ElementKind.CLASS && !classElement.getModifiers().contains(Modifier.ABSTRACT);
    }
}
