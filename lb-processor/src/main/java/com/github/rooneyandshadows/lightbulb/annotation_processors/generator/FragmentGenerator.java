package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.RemoveField;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

public class FragmentGenerator extends CodeGenerator {

    public FragmentGenerator(String rootPackage, Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(rootPackage, filer, annotationResultsRegistry);
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(FRAGMENT_BINDINGS);
        generateBindings(fragmentBindings);
    }

    private void generateBindings(List<FragmentBindingData> fragmentBindings) {
        fragmentBindings.forEach(fragmentInfo -> {
            List<MethodSpec> methods = new ArrayList<>();
            List<FieldSpec> fields = new ArrayList<>();
            fragmentInfo.getParameters().forEach(parameter -> {
                FieldSpec fieldSpec = FieldSpec.builder(parameter.getType(), parameter.getName(), PROTECTED)
                        .addAnnotation(RemoveField.class)
                        .build();
                fields.add(fieldSpec);
            });

            fragmentInfo.getPersistedVariables().forEach(variable -> {
                FieldSpec fieldSpec = FieldSpec.builder(variable.getType(), variable.getName(), PROTECTED)
                        .addAnnotation(RemoveField.class)
                        .build();
                fields.add(fieldSpec);
            });


            generateFragmentNewInstanceMethods(fragmentInfo, methods);
            generateOnCreateMethod(fragmentInfo, methods);
            generateOnCreateViewMethod(fragmentInfo, methods);
            generateOnViewCreatedMethod(fragmentInfo, methods);
            generateOnSaveInstanceStateMethod(fragmentInfo, methods);
            generateFragmentParametersMethod(fragmentInfo, methods);
            generateSaveVariablesMethod(fragmentInfo, methods);
            generateRestoreVariablesMethod(fragmentInfo, methods);
            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(fragmentInfo.getBindingClassName())
                    .addModifiers(PUBLIC)
                    .addFields(fields)
                    .superclass(fragmentInfo.getSuperClassName())
                    .addMethods(methods);
            try {
                JavaFile.builder(fragmentInfo.getBindingClassName().packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateFragmentNewInstanceMethods(FragmentBindingData fragmentInfo, List<MethodSpec> destination) {
        if (!fragmentInfo.isCanBeInstantiated()) {
            return;
        }

        if (fragmentInfo.hasOptionalParameters()) {
            generateFragmentNewInstanceMethod(fragmentInfo, false, destination);
        }

        generateFragmentNewInstanceMethod(fragmentInfo, true, destination);
    }

    private void generateFragmentNewInstanceMethod(
            FragmentBindingData fragmentInfo,
            boolean includeOptionalParams,
            List<MethodSpec> destination
    ) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(PUBLIC, STATIC)
                .returns(fragmentInfo.getClassName())
                .addStatement("$T fragment = new $T()", fragmentInfo.getClassName(), fragmentInfo.getClassName())
                .addStatement("$T arguments = new $T()", ANDROID_BUNDLE, ANDROID_BUNDLE);

        fragmentInfo.getFragmentParameters(includeOptionalParams).forEach(param -> {
            builder.addParameter(param.getParameterSpec());
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "arguments", null);
            builder.addCode(writeStatement);
        });

        builder.addStatement("fragment.setArguments(arguments)");
        builder.addStatement("return fragment");

        destination.add(builder.build());
    }

    private void generateOnCreateMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        boolean hasParameters = !fragment.getParameters().isEmpty();
        boolean hasPersistedVars = !fragment.getParameters().isEmpty();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (hasParameters && hasPersistedVars) {
            builder.beginControlFlow("if(savedInstanceState == null)")
                    .addStatement("$T arguments = getArguments()", ANDROID_BUNDLE)
                    .addStatement("generateParameters(arguments)")
                    .nextControlFlow("else")
                    .addStatement("restoreVariablesState(savedInstanceState)")
                    .endControlFlow();
        } else if (hasParameters) {
            builder.beginControlFlow("if(savedInstanceState == null)")
                    .addStatement("$T arguments = getArguments()", ANDROID_BUNDLE)
                    .addStatement("generateParameters(arguments)")
                    .endControlFlow();
        } else {
            builder.beginControlFlow("if(savedInstanceState != null)")
                    .addStatement("restoreVariablesState(savedInstanceState)")
                    .endControlFlow();
        }


        destination.add(builder.build());
    }

    private void generateOnCreateViewMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        Configuration configuration = fragment.getConfiguration();
        String layoutName = configuration.getLayoutName();

        if (StringUtils.isNullOrEmptyString(layoutName)) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateView")
                .addModifiers(PUBLIC)
                .addParameter(ANDROID_LAYOUT_INFLATER, "inflater")
                .addParameter(ANDROID_VIEW_GROUP, "container")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .returns(ANDROID_VIEW)
                .addStatement("super.onCreateView(inflater,container,savedInstanceState)")
                .addStatement("$T layout = inflater.inflate($T.layout.$L, null)", ANDROID_VIEW, ANDROID_R, layoutName)
                .addStatement("return layout");

        destination.add(builder.build());
    }

    private void generateOnViewCreatedMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        if (fragment.getViewBindings().isEmpty()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addParameter(ANDROID_VIEW, "fragmentView")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addModifiers(PUBLIC)
                .addStatement("super.onViewCreated(fragmentView,savedInstanceState)")
                .returns(void.class);

        fragment.getViewBindings().forEach(bindingInfo -> {
            String fieldName = bindingInfo.getFieldName();
            String fieldSetterName = bindingInfo.getSetterName();
            String resourceName = bindingInfo.getResourceName();
            builder.addStatement("$T view = fragment.getView()", ANDROID_VIEW);
            if (bindingInfo.hasSetter()) {
                String statement = "fragment.$L(view.findViewById($T.id.$L))";
                builder.addStatement(statement, fieldSetterName, ANDROID_R, resourceName);
            } else {
                String statement = "fragment.$L = view.findViewById($T.id.$L)";
                builder.addStatement(statement, fieldName, ANDROID_R, resourceName);
            }
        });

        destination.add(builder.build());
    }

    private void generateOnSaveInstanceStateMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        boolean hasParameters = !fragment.getParameters().isEmpty();
        boolean hasPersistedVars = !fragment.getParameters().isEmpty();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onSaveInstanceState")
                .addParameter(ANDROID_BUNDLE, "outState")
                .addModifiers(PUBLIC)
                .addStatement("super.onSaveInstanceState(outState)")
                .addStatement("saveVariablesState(outState)")
                .returns(void.class);

        destination.add(builder.build());
    }

    private void generateFragmentParametersMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("generateParameters")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "arguments")
                .returns(void.class);

        if (fragment.getParameters().isEmpty()) {
            return;
        }

        fragment.getParameters().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "arguments", "", true);
            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }

    private void generateSaveVariablesMethod(FragmentBindingData fragmentInfo, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "outState")
                .returns(void.class);

        if (fragmentInfo.getParameters().isEmpty() && fragmentInfo.getPersistedVariables().isEmpty()) {
            return;
        }

        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "outState", "");
            builder.addCode(writeStatement);
        });

        fragmentInfo.getPersistedVariables().forEach(param -> {
            CodeBlock writeStatement = generatePutIntoBundleBlockForParam(param, "outState", "");
            builder.addCode(writeStatement);
        });

        destination.add(builder.build());
    }

    private void generateRestoreVariablesMethod(FragmentBindingData fragmentInfo, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "fragmentSavedInstanceState")
                .returns(void.class);

        if (fragmentInfo.getParameters().isEmpty() && fragmentInfo.getPersistedVariables().isEmpty()) {
            return;
        }

        fragmentInfo.getParameters().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "fragmentSavedInstanceState", "", false);
            builder.addCode(readStatement);
        });

        fragmentInfo.getPersistedVariables().forEach(param -> {
            CodeBlock readStatement = generateReadFromBundleBlockForParam(param, "fragmentSavedInstanceState", "", false);
            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }
}