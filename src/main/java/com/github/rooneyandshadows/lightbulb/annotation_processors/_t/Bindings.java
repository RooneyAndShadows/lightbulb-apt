package com.github.rooneyandshadows.lightbulb.annotation_processors._t;

import androidx.fragment.app.FragmentActivity;

import java.util.HashMap;

class Bindings {
    private static Bindings single_instance = null;
    private final HashMap<Class<? extends FragmentActivity>, BindingWrapper<? extends Binding>> bindings = new HashMap<>();

    private Bindings() {
    }

    private static synchronized Bindings getInstance() {
        if (single_instance == null) {
            single_instance = new Bindings();
        }
        single_instance.bindings.put(MainActivity.class, new BindingWrapper<>(TestBinding::new));
        return single_instance;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Binding> T getBindingForClass(Class<? extends FragmentActivity> target) {
        final BindingWrapper<T> bindingWrapper = (BindingWrapper<T>) getInstance().bindings.get(target);
        if (bindingWrapper == null) {
            return null;
        }
        return bindingWrapper.getBinding();
    }
}