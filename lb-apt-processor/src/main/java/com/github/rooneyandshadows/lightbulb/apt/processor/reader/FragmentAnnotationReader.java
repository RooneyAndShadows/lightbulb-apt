package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.ViewBinding;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_FRAGMENT_DESCRIPTION;
import static javax.lang.model.element.ElementKind.*;

public class FragmentAnnotationReader extends AnnotationReader {
    private final List<LightbulbFragmentDescription> fragmentBindings = new ArrayList<>();

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
            }
            if (annotation instanceof FragmentScreen fragmentScreen) {
                fragmentDataBuilder.withScreenName(fragmentScreen.screenName());
                fragmentDataBuilder.withScreenGroupName(fragmentScreen.screenGroup());
            }
            if (annotation instanceof FragmentParameter fragmentParameter) {
                Parameter parameter = new Parameter(element.getElement(), fragmentParameter.optional());
                fragmentDataBuilder.withParameter(parameter);
            }
            if (annotation instanceof FragmentStatePersisted fragmentStatePersisted) {
                Field variableInfo = new Field(element.getElement());
                fragmentDataBuilder.withPersistedVariable(variableInfo);
            }
            if (annotation instanceof BindView bindView) {
                ViewBinding viewBindingInfo = new ViewBinding(element.getElement(), bindView.name());
                fragmentDataBuilder.withViewBinding(viewBindingInfo);
            }
        });

        fragmentBindings.add(fragmentDataBuilder.build());
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_FRAGMENT_DESCRIPTION, fragmentBindings);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbFragment.class, CLASS);
        targets.put(FragmentScreen.class, CLASS);
        targets.put(FragmentParameter.class, FIELD);
        targets.put(FragmentStatePersisted.class, FIELD);
        targets.put(BindView.class, FIELD);
        return targets;
    }
}
