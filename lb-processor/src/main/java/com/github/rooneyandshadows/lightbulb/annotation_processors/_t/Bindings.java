package com.github.rooneyandshadows.lightbulb.annotation_processors._t;


import com.example.myapplication.Binding;

import java.util.HashMap;

class Bindings {
    private static final Bindings single_instance = new Bindings();
    private final HashMap<Class<?>, BindingWrapper<? extends Binding>> bindings = new HashMap<>();

    private Bindings() {
        //single_instance.bindings.put(MainActivity.class, new BindingWrapper<>(TestBinding::new));
    }

    private static synchronized Bindings getInstance() {
        return single_instance;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Binding> T getBindingForClass(Class<?> target) {
        final BindingWrapper<T> bindingWrapper = (BindingWrapper<T>) getInstance().bindings.get(target);
        if (bindingWrapper == null) {
            return null;
        }
        return bindingWrapper.getBinding();
    }
}