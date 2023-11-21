package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbTransformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Variable;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

//TODO ADD ANNOTATIONS FOR BYTECODE TRANSFORMATIONS
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.LB_TRANSFORMATION_ANNOTATION;
import static javax.lang.model.element.Modifier.*;

public class FragmentGenerator extends CodeGenerator {
    protected final ClassName ANDROID_R;

    public FragmentGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        this.ANDROID_R = ClassNames.androidResources();
    }

    @Override
    public void generate() {
        List<FragmentBindingData> fragmentBindings = annotationResultsRegistry.getResult(AnnotationResultsRegistry.AnnotationResultTypes.FRAGMENT_BINDINGS);
        generateFragments(fragmentBindings);
    }

    private void generateFields(FragmentBindingData fragment, List<FieldSpec> destination) {
        List<Variable> fields = new ArrayList<>();
        fields.addAll(fragment.getParameters());
        fields.addAll(fragment.getPersistedVariables());
        fields.forEach(variable -> {
            Modifier modifier = variable.accessModifierAtLeast(PROTECTED) ? variable.getAccessModifier() : PROTECTED;
            FieldSpec fieldSpec = FieldSpec.builder(variable.getType(), variable.getName(), modifier)
                    .build();
            destination.add(fieldSpec);
        });
    }

    private void generateFragments(List<FragmentBindingData> fragmentBindings) {
        fragmentBindings.forEach(fragmentInfo -> {
            List<MethodSpec> methods = new ArrayList<>();
            List<FieldSpec> fields = new ArrayList<>();

            generateFields(fragmentInfo, fields);
            generateOnCreateMethod(fragmentInfo, methods);
            generateOnCreateViewMethod(fragmentInfo, methods);
            generateOnViewCreatedMethod(fragmentInfo, methods);
            generateOnSaveInstanceStateMethod(fragmentInfo, methods);
            generateFragmentParametersMethod(fragmentInfo, methods);
            generateSaveVariablesMethod(fragmentInfo, methods);
            generateRestoreVariablesMethod(fragmentInfo, methods);

            AnnotationSpec annotationSpec =  AnnotationSpec.builder(LB_TRANSFORMATION_ANNOTATION)
                    .addMember("target", "$T.class", fragmentInfo.getClassName())
                    .build();

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(fragmentInfo.getInstrumentedClassName())
                    .addModifiers(PUBLIC)
                    .addAnnotation(annotationSpec)
                    .addFields(fields)
                    .superclass(fragmentInfo.getSuperClassName())
                    .addMethods(methods);
            try {
                JavaFile.builder(fragmentInfo.getInstrumentedClassName().packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateOnCreateMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        boolean hasParameters = !fragment.getParameters().isEmpty();
        boolean hasPersistedVars = !fragment.getParameters().isEmpty();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (hasParameters && hasPersistedVars) {
            builder.beginControlFlow("if(savedInstanceState == null)")
                    .addStatement("$T arguments = getArguments()", ClassNames.ANDROID_BUNDLE)
                    .addStatement("generateParameters(arguments)")
                    .nextControlFlow("else")
                    .addStatement("restoreVariablesState(savedInstanceState)")
                    .endControlFlow();
        } else if (hasParameters) {
            builder.beginControlFlow("if(savedInstanceState == null)")
                    .addStatement("$T arguments = getArguments()", ClassNames.ANDROID_BUNDLE)
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
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_LAYOUT_INFLATER, "inflater")
                .addParameter(ClassNames.ANDROID_VIEW_GROUP, "container")
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .returns(ClassNames.ANDROID_VIEW)
                .addStatement("super.onCreateView(inflater,container,savedInstanceState)")
                .addStatement("$T layout = inflater.inflate($T.layout.$L, null)", ClassNames.ANDROID_VIEW, ANDROID_R, layoutName)
                .addStatement("return layout");

        destination.add(builder.build());
    }

    private void generateOnViewCreatedMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        if (fragment.getViewBindings().isEmpty()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_VIEW, "fragmentView")
                .addParameter(ClassNames.ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onViewCreated(fragmentView,savedInstanceState)")
                .returns(void.class);

        fragment.getViewBindings().forEach(bindingInfo -> {
            String fieldName = bindingInfo.getFieldName();
            String fieldSetterName = bindingInfo.getSetterName();
            String resourceName = bindingInfo.getResourceName();
            builder.addStatement("$T view = fragment.getView()", ClassNames.ANDROID_VIEW);
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
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassNames.ANDROID_BUNDLE, "outState")
                .addStatement("super.onSaveInstanceState(outState)")
                .addStatement("saveVariablesState(outState)")
                .returns(void.class);

        destination.add(builder.build());
    }

    private void generateFragmentParametersMethod(FragmentBindingData fragment, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("generateParameters")
                .addModifiers(PRIVATE)
                .addParameter(ClassNames.ANDROID_BUNDLE, "arguments")
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
                .addParameter(ClassNames.ANDROID_BUNDLE, "outState")
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
                .addParameter(ClassNames.ANDROID_BUNDLE, "fragmentSavedInstanceState")
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