package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.squareup.javapoet.TypeName;

public class FragmentParamInfo {
        private final String name;
        private final TypeName type;
        private final boolean optional;

        public FragmentParamInfo(String name, TypeName type, boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
        }

        public String getName() {
            return name;
        }

        public TypeName getType() {
            return type;
        }

        public boolean isOptional() {
            return optional;
        }
    }