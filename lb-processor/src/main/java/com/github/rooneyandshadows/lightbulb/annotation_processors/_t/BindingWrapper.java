package com.github.rooneyandshadows.lightbulb.annotation_processors._t;

import com.example.myapplication.Binding;

class BindingWrapper<T extends Binding> {
    private final BindingInitializer<T> initializer;
    private T binding = null;

    public BindingWrapper(BindingInitializer<T> initializer) {
        this.initializer = initializer;
    }

    public T getBinding() {
        if (binding == null) {
            binding = initializer.initialize();
        }
        return binding;
    }
}