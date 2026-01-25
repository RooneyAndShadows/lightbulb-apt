package com.github.rooneyandshadows.lightbulb.apt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface FragmentViewModel {
    ViewModelScope scope() default ViewModelScope.LOCAL_FRAGMENT;

    enum ViewModelScope {
        ACTIVITY,
        PARENT_FRAGMENT,
        LOCAL_FRAGMENT
    }
}