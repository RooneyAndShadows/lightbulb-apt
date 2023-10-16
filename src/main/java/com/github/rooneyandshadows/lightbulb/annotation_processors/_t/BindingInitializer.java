package com.github.rooneyandshadows.lightbulb.annotation_processors._t;

import com.example.myapplication.Binding;

@FunctionalInterface
interface BindingInitializer<T extends Binding> {
    public T initialize();
}