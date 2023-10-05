package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface FragmentParameter {
    boolean optional() default false;
}