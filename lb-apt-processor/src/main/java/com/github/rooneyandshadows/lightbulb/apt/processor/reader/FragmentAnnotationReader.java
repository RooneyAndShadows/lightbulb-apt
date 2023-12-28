package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldPersisted;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.FieldViewBinding;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_FRAGMENT_DESCRIPTION;
import static javax.lang.model.element.ElementKind.*;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<LightbulbFragmentDescription> fragmentDescriptions = new ArrayList<>();

    public FragmentAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        LightbulbFragmentDescription.Builder fragmentDataBuilder = new LightbulbFragmentDescription.Builder(elements, target);

        annotatedElements.forEach(element -> {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof LightbulbFragment lightbulbFragment) {
                fragmentDataBuilder.withLayoutName(lightbulbFragment.layoutName());
            } else if (annotation instanceof FragmentScreen fragmentScreen) {
                fragmentDataBuilder.withScreenName(fragmentScreen.screenName());
                fragmentDataBuilder.withScreenGroupName(fragmentScreen.screenGroup());
            } else if (annotation instanceof com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter fragmentParameter) {
                FieldScreenParameter parameter = new FieldScreenParameter(element.getElement(), fragmentParameter.optional());
                fragmentDataBuilder.withParameter(parameter);
            } else if (annotation instanceof FragmentStatePersisted fragmentStatePersisted) {
                FieldPersisted variableInfo = new FieldPersisted(element.getElement());
                fragmentDataBuilder.withPersistedVariable(variableInfo);
            } else if (annotation instanceof BindView bindView) {
                FieldViewBinding viewBindingInfo = new FieldViewBinding(element.getElement(), bindView.name());
                fragmentDataBuilder.withViewBinding(viewBindingInfo);
            }
        });

        fragmentDescriptions.add(fragmentDataBuilder.build());
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_FRAGMENT_DESCRIPTION, fragmentDescriptions);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbFragment.class, CLASS);
        targets.put(FragmentScreen.class, CLASS);
        targets.put(com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter.class, FIELD);
        targets.put(FragmentStatePersisted.class, FIELD);
        targets.put(BindView.class, FIELD);
        return targets;
    }
}
