package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.activity.ActivityInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.CodeGenerator;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.ProcessorOptionNames.ROOT_PACKAGE;

@SuppressWarnings("FieldCanBeLocal")
@AutoService(Processor.class)
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
        boolean processResult;
        //Set<? extends Element> rootE=roundEnvironment.getRootElements();
        //for(Element e: rootE) {
        //    for(Element subElement : e.getEnclosedElements()){
        //        System.out.println(subElement);
        //    }
        //}
        AnnotationReader reader = new AnnotationReader(messager, elements);
        processResult = reader.obtainAnnotatedClassesWithActivityConfiguration(roundEnvironment);
        processResult &= reader.obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment);
        processResult &= reader.obtainAnnotatedClassesWithFragmentScreen(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithBindView(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment);
        if (!processResult) return false;
        List<FragmentInfo> fragmentInfoList = reader.getFragmentInfoList();
        List<ActivityInfo> activityInfoList = reader.getActivityInfoList();
        List<FragmentScreenGroup> screenGroups = reader.getScreenGroups();
        CodeGenerator generator = new CodeGenerator(rootPackage, filer);
        generator.generateFragmentBindingClasses(fragmentInfoList);
        activityInfoList.forEach(activityInfo -> {
            if (!activityInfo.isRoutingEnabled())
                return;
            generator.generateRoutingScreens(screenGroups);
            generator.generateRouterClass(activityInfo.getClassName(), screenGroups);
        });
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(ActivityConfiguration.class.getCanonicalName());
                add(BindView.class.getCanonicalName());
                add(FragmentConfiguration.class.getCanonicalName());
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