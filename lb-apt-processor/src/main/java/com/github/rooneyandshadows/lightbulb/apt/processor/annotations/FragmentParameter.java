package com.github.rooneyandshadows.lightbulb.apt.processor.annotations;

import java.lang.annotation.*;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface FragmentParameter {
    boolean optional() default false;
}