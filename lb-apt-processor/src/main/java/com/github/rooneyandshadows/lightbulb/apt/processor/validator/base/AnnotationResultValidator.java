package com.github.rooneyandshadows.lightbulb.apt.processor.validator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;

import javax.annotation.processing.Messager;
import java.util.Locale;

@SuppressWarnings("SameParameterValue")
public abstract class AnnotationResultValidator {
    private final Messager messager;
    private final AnnotationResultsRegistry annotationResultsRegistry;

    public AnnotationResultValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        this.messager = messager;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    protected abstract boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry);

    public final boolean validate() {
        return validateResult(messager, annotationResultsRegistry);
    }

    protected final static class ErrorString {
        private String errorString = "";

        public ErrorString(String format, Object... args) {
            append(format, args);
        }

        public ErrorString(String string) {
            append(string);
        }

        public void append(String format, Object... args) {
            errorString = errorString.concat(String.format(Locale.getDefault(), format, args));
        }

        public void append(String string) {
            errorString = errorString.concat(string);
        }

        public String getErrorString() {
            return errorString;
        }
    }
}
