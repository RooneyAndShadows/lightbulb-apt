package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbFragment;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.FragmentStatePersisted;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Variable;
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
            consumeAnnotation(LightbulbFragment.class, element, lightbulbFragment -> {
                fragmentDataBuilder.withLayoutName(lightbulbFragment.layoutName());
            });
            consumeAnnotation(FragmentScreen.class, element, fragmentScreen -> {
                fragmentDataBuilder.withScreenName(fragmentScreen.screenName());
                fragmentDataBuilder.withScreenGroupName(fragmentScreen.screenGroup());
            });
            consumeAnnotation(FragmentParameter.class, element, fragmentParameter -> {
                Parameter parameter = new Parameter(element.getElement(), fragmentParameter.optional());
                fragmentDataBuilder.withParameter(parameter);
            });
            consumeAnnotation(FragmentStatePersisted.class, element, fragmentStatePersisted -> {
                Variable variableInfo = new Variable(element.getElement());
                fragmentDataBuilder.withPersistedVariable(variableInfo);
            });
            consumeAnnotation(BindView.class, element, bindView -> {
                ViewBinding viewBindingInfo = new ViewBinding(element.getElement(), bindView.name());
                fragmentDataBuilder.withViewBinding(viewBindingInfo);
            });
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
