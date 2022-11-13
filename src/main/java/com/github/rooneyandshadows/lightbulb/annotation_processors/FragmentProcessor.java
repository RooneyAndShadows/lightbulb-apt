package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentParamInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.CodeGenerator;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames.GENERATED_LB_SCREENS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.*;

@SuppressWarnings("FieldCanBeLocal")
@AutoService(Processor.class)
public class FragmentProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
        this.elements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //Get annotated targets
        boolean processResult;
        AnnotationReader reader = new AnnotationReader(messager, elements);
        processResult = reader.obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment);
        processResult &= reader.obtainAnnotatedClassesWithFragmentScreen(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithBindView(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment);
        if (!processResult) {
            return false;
        }
        List<FragmentInfo> fragmentInfoList = reader.getFragmentInfoList();
        List<FragmentScreenGroup> screenGroups = reader.getScreenGroups();
        CodeGenerator.generateFragmentBindingClasses(filer, fragmentInfoList);
        CodeGenerator.generateRoutingScreens(filer, screenGroups);
        CodeGenerator.generateRouterClass(filer, screenGroups);
        //Generate methods
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(BindView.class.getCanonicalName());
                add(FragmentConfiguration.class.getCanonicalName());
                add(FragmentParameter.class.getCanonicalName());
                add(FragmentScreen.class.getCanonicalName());
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}