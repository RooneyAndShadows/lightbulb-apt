package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base;

import javax.annotation.processing.Filer;

public class CodeGenerator {
    protected final String rootPackage;
    protected final Filer filer;

    public CodeGenerator(String rootPackage, Filer filer) {
        this.rootPackage = rootPackage;
        this.filer = filer;
    }
}
