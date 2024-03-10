package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.BaseDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.TypeElement;
import java.util.List;


public final class ClassDefinition extends BaseDefinition<TypeElement> {
    private final String name;
    private final TypeDefinition type;
    private final List<MethodDefinition> methods;
    private final List<FieldDefinition> fields;

    public ClassDefinition(TypeElement typeElement) {
        super(typeElement);
        this.name = typeElement.getSimpleName().toString();
        this.type = new TypeDefinition(typeElement.asType());
        this.methods = MethodDefinition.from(ElementUtils.getMethods(typeElement));
        this.fields = FieldDefinition.from(ElementUtils.getFields(typeElement));
    }

    public String getName() {
        return name;
    }

    public TypeDefinition getType() {
        return type;
    }

    public List<MethodDefinition> getMethods() {
        return methods;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }
}