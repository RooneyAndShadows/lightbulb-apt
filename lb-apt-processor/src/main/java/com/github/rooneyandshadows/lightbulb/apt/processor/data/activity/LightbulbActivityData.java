package com.github.rooneyandshadows.lightbulb.apt.processor.data.activity;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.LightbulbFragmentData;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_CLASS_NAME_PREFIX;

public record LightbulbActivityData(
        ClassName className,
        ClassName superClassName,
        ClassName instrumentedClassName,
        boolean routingEnabled,
        String fragmentContainerId
) {

    private void handleActivityConfiguration(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof LightbulbActivity config)) return;
        routingEnabled = config.enableRouterGeneration();
        fragmentContainerId = config.fragmentContainerId();
    }

    public static final class Builder {
        private final ClassName className;
        private final ClassName superClassName;
        private final ClassName instrumentedClassName;
        private boolean routingEnabled;
        private String fragmentContainerId;

        public Builder(Elements elements, TypeElement fragmentClassElement) {
            this.className = ClassNames.generateClassName(fragmentClassElement, elements);
            this.superClassName = ClassNames.generateSuperClassName(fragmentClassElement, elements);
            this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(PackageNames.getFragmentsPackage(), className.simpleName(), GENERATED_CLASS_NAME_PREFIX);
        }

        public LightbulbActivityData build() {
            return new LightbulbActivityData(
                    className,
                    superClassName,
                    instrumentedClassName,
                    routingEnabled,
                    fragmentContainerId
            );
        }

        public void withRoutingEnabled(boolean routingEnabled) {
            this.routingEnabled = routingEnabled;
        }

        public void withFragmentContainerId(String fragmentContainerId) {
            this.fragmentContainerId = fragmentContainerId;
        }
    }
}