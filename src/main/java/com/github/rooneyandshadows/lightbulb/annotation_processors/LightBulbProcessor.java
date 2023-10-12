package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.activity.ActivityGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.activity.data.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.activity.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data.inner.ScreenInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.FragmentGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.ActivityAnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.FragmentAnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.routing.RoutingGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.*;

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
    private RoutingGenerator routingGenerator;
    private ActivityGenerator activityGenerator;
    private FragmentGenerator fragmentGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
        this.elements = processingEnvironment.getElementUtils();
        this.options = processingEnvironment.getOptions();
        routingGenerator = new RoutingGenerator(rootPackage, filer);
        fragmentGenerator = new FragmentGenerator(rootPackage, filer);
        activityGenerator = new ActivityGenerator();


    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        rootPackage = getRootPackage();

//-------------
        List<AnnotationReader> readers = new ArrayList<>();
        AnnotationResultsRegistry resultsRegistry = new AnnotationResultsRegistry();
        readers.add(new ActivityAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new FragmentAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.forEach(AnnotationReader::readAnnotations);
//-------------
        boolean processResult;
        ActivityAnnotationReader activityAnnotationReader = new ActivityAnnotationReader(messager, elements, roundEnvironment);
        FragmentAnnotationReader fragmentAnnotationReader = new FragmentAnnotationReader(messager, elements, roundEnvironment);

        processResult = activityAnnotationReader.readAnnotations();
        processResult &= fragmentAnnotationReader.readAnnotations();
        if (!processResult) return false;


        List<ActivityBindingData> activityInfoList = activityAnnotationReader.getActivityBindings();
        List<FragmentBindingData> fragmentInfoList = fragmentAnnotationReader.getFragmentBindings();


        fragmentGenerator.generateFragmentBindingClasses(fragmentInfoList);
        routingGenerator.generateRoutingClasses(activityInfoList, fragmentInfoList);

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