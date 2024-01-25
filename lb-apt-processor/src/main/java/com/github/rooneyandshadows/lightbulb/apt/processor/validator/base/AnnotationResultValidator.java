package com.github.rooneyandshadows.lightbulb.apt.processor.validator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;

import javax.annotation.processing.Messager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

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

    protected static class ValidateResult {
        private final String heading;
        private final List<ErrorLine> errors = new ArrayList<>();

        public ValidateResult(String initialErrorString) {
            this.heading = initialErrorString;
        }

        public void addError(ErrorLine errorLine) {
            errors.add(errorLine);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }


        public String getErrorText() {
            return printRecursively(heading, errors, "", 1);
        }

        private String printRecursively(String target, List<ErrorLine> errors, String prefix, int indent) {
            for (int index = 0; index < errors.size(); index++) {
                ErrorLine errorLine = errors.get(index);
                String currentPrefix = format(Locale.getDefault(), "\n%" + indent * 4 + "s%s%d.", prefix, index++);

                target = target.concat(format("%s %s", currentPrefix, errorLine.errorText));

                if (!errorLine.subErrors.isEmpty()) {
                    target = printRecursively(target, errorLine.subErrors, currentPrefix, indent + 1);
                }
            }
            return target;
        }

        public static final class ErrorLine {
            private final String errorText;
            private final List<ErrorLine> subErrors = new ArrayList<>();

            public ErrorLine(String errorText) {
                this.errorText = errorText;
            }

            public void addSubError(ErrorLine errorLine) {
                subErrors.add(errorLine);
            }
        }
    }
}
