package com.github.rooneyandshadows.lightbulb.apt.processor;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ProcessorOptionNames.PROJECT_ROOT_PACKAGE;

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
        String rootPackage = getRootPackage();
        PackageNames.init(rootPackage);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        CodeGenerationService generationService = new CodeGenerationService(filer, messager, elements, roundEnvironment);
        generationService.process();
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(LightbulbActivity.class.getCanonicalName());
                add(BindView.class.getCanonicalName());
                add(LightbulbFragment.class.getCanonicalName());
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
                add(PROJECT_ROOT_PACKAGE);
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getRootPackage() {
        String rootPackage = options.get(PROJECT_ROOT_PACKAGE);
        if (rootPackage == null || rootPackage.equals("")) {
            String className = getClass().getSimpleName();
            String message = className.concat(": ")
                    .concat("Failed to generate sources.")
                    .concat("Please provide \"")
                    .concat(PROJECT_ROOT_PACKAGE)
                    .concat("\" argument in annotationProcessorOptions.");
            messager.printMessage(Diagnostic.Kind.ERROR, message);
        }
        return rootPackage;
    }
}