package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.activity.ActivityInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentScreenGroup;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.CodeGenerator;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("FieldCanBeLocal")
@AutoService(Processor.class)
public class FragmentProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Map<String, String> options;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
        this.elements = processingEnvironment.getElementUtils();
        this.options = processingEnvironment.getOptions();

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //Get annotated targets
        boolean processResult;
        AnnotationReader reader = new AnnotationReader(messager, elements);
        processResult = reader.obtainAnnotatedClassesWithActivityConfiguration(roundEnvironment);
        processResult &= reader.obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment);
        processResult &= reader.obtainAnnotatedClassesWithFragmentScreen(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithBindView(roundEnvironment);
        processResult &= reader.obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment);
        if (!processResult) return false;
        options.forEach((s, s2) -> {
            messager.printMessage(Diagnostic.Kind.WARNING, s.concat(":").concat(s2));
        });
        List<FragmentInfo> fragmentInfoList = reader.getFragmentInfoList();
        List<ActivityInfo> activityInfoList = reader.getActivityInfoList();
        List<FragmentScreenGroup> screenGroups = reader.getScreenGroups();
        CodeGenerator.generateFragmentBindingClasses(filer, fragmentInfoList);
        activityInfoList.forEach(activityInfo -> {
            if (!activityInfo.isRoutingEnabled())
                return;
            CodeGenerator.generateRoutingScreens(filer, screenGroups);
            CodeGenerator.generateRouterClass(filer, activityInfo.getClassName(), screenGroups);
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
                add("key1");
                add("key2");
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}