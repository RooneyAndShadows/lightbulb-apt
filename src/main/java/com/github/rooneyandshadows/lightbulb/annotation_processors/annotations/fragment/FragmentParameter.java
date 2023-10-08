package com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.fragment;

import java.lang.annotation.*;


@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface FragmentParameter {
    boolean optional() default false;
}