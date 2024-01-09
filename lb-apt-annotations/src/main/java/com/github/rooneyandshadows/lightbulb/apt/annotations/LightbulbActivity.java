package com.github.rooneyandshadows.lightbulb.apt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface LightbulbActivity {
    String fragmentContainerId() default "fragmentContainer";
}