package com.github.rooneyandshadows.lightbulb.apt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface FragmentViewModel {
    ViewModelScope scope() default ViewModelScope.FRAGMENT;

    enum ViewModelScope {
        ACTIVITY,
        FRAGMENT
    }
}