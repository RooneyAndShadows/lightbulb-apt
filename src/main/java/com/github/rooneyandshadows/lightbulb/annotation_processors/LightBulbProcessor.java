package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentBindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentStatePersisted;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ProcessorOptionNames.ROOT_PACKAGE;

@SuppressWarnings("FieldCanBeLocal")
@AutoService(Processor.class)
//TODO ADD GENERATION FOR PARCELABLE OBJECTS (auto created parcelling part)
public class LightBulbProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Map<String, String> options;
    private String rootPackage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
        this.elements = processingEnvironment.getElementUtils();
        this.options = processingEnvironment.getOptions();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        rootPackage = getRootPackage();
        CodeGenerationService generationService = new CodeGenerationService(rootPackage, filer, messager, elements, roundEnvironment);
        generationService.process();
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(ActivityConfiguration.class.getCanonicalName());
                add(FragmentBindView.class.getCanonicalName());
                add(FragmentConfiguration.class.getCanonicalName());
                add(FragmentStatePersisted.class.getCanonicalName());
                add(FragmentParameter.class.getCanonicalName());
                add(FragmentScreen.class.getCanonicalName());
            }
        };
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {
            {
                add(ROOT_PACKAGE);
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getRootPackage() {
        String rootPackage = options.get(ROOT_PACKAGE);
        if (rootPackage == null || rootPackage.equals("")) {
            String className = getClass().getSimpleName();
            String message = className.concat(": ")
                    .concat("Failed to generate sources.")
                    .concat("Please provide \"")
                    .concat(ROOT_PACKAGE)
                    .concat("\" argument in annotationProcessorOptions.");
            messager.printMessage(Diagnostic.Kind.ERROR, message);
        }
        return rootPackage;
    }
}