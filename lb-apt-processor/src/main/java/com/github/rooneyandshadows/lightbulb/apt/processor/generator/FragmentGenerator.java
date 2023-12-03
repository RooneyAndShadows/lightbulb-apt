package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.FragmentBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Configuration;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Variable;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO ADD ANNOTATIONS FOR BYTECODE TRANSFORMATIONS
import static com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator.generateReadStatement;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.ILLEGAL_ARGUMENT_EXCEPTION;
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

    private void generateFragments(List<FragmentBindingData> fragmentBindings) {
        fragmentBindings.forEach(fragmentInfo -> {
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(fragmentInfo, fields, methods);
            generateOnCreateMethod(fragmentInfo, methods);
            generateOnCreateViewMethod(fragmentInfo, methods);
            generateOnViewCreatedMethod(fragmentInfo, methods);
            generateOnSaveInstanceStateMethod(fragmentInfo, methods);
            generateFragmentParametersMethod(fragmentInfo, methods);
            generateSaveVariablesMethod(fragmentInfo, methods);
            generateRestoreVariablesMethod(fragmentInfo, methods);

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(fragmentInfo.getInstrumentedClassName())
                    .addModifiers(PUBLIC, ABSTRACT)
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

    private void generateFields(FragmentBindingData fragment, List<FieldSpec> fields, List<MethodSpec> methods) {
        List<Variable> targets = new ArrayList<>();
        targets.addAll(fragment.getParameters());
        targets.addAll(fragment.getPersistedVariables());
        targets.addAll(fragment.getViewBindings());
        
        targets.forEach(variable -> {
            boolean hasSetter = variable.getSetterName() != null;
            boolean hasGetter = variable.getGetterName() != null;

            if (!hasGetter || !hasSetter) {
                Modifier fieldAccessModifier = variable.accessModifierAtLeast(PROTECTED) ? variable.getAccessModifier() : PROTECTED;
                FieldSpec fieldSpec = FieldSpec.builder(variable.getType(), variable.getName(), fieldAccessModifier)
                        .build();
                fields.add(fieldSpec);
            }

            if (hasGetter) {
                Modifier access = variable.getGetterAccessModifier() == PRIVATE ? PROTECTED : variable.getGetterAccessModifier();

                MethodSpec getter = MethodSpec.methodBuilder(variable.getGetterName())
                        .returns(variable.getType())
                        .addModifiers(access, ABSTRACT)
                        .build();

                methods.add(getter);
            }

            if (hasSetter) {
                Modifier access = variable.getSetterAccessModifier() == PRIVATE ? PROTECTED : variable.getSetterAccessModifier();

                MethodSpec setter = MethodSpec.methodBuilder(variable.getSetterName())
                        .addParameter(variable.getType(), "value")
                        .addModifiers(access, ABSTRACT)
                        .build();

                methods.add(setter);
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

        if (layoutName == null || layoutName.isBlank()) {
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
            String fieldName = bindingInfo.getName();
            String fieldSetterName = bindingInfo.getSetterName();
            String resourceName = bindingInfo.getResourceName();
            builder.addStatement("$T view = getView()", ClassNames.ANDROID_VIEW);
            if (bindingInfo.hasSetter()) {
                String statement = "$L(view.findViewById($T.id.$L))";
                builder.addStatement(statement, fieldSetterName, ANDROID_R, resourceName);
            } else {
                String statement = "$L = view.findViewById($T.id.$L)";
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
        String argumentsParameterName = "arguments";

        MethodSpec.Builder builder = MethodSpec.methodBuilder("generateParameters")
                .addModifiers(PRIVATE)
                .addParameter(ClassNames.ANDROID_BUNDLE, argumentsParameterName)
                .returns(void.class);

        if (fragment.getParameters().isEmpty()) {
            return;
        }

        fragment.getParameters().forEach(param -> {
            CodeBlock readStatement = generateReadParameterFromBundle(param, argumentsParameterName);
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

        List<Variable> fieldsToSave = new ArrayList<>();
        fieldsToSave.addAll(fragmentInfo.getPersistedVariables());
        fieldsToSave.addAll(fragmentInfo.getParameters());

        if (fieldsToSave.isEmpty()) {
            return;
        }

        fieldsToSave.forEach(field -> {
            CodeBlock writeStatement = generateWriteIntoBundleBlock(field, "outState", "this", true);
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

        List<Variable> fieldsToRestore = new ArrayList<>();
        fieldsToRestore.addAll(fragmentInfo.getParameters());
        fieldsToRestore.addAll(fragmentInfo.getPersistedVariables());

        fieldsToRestore.forEach(field -> {
            CodeBlock readStatement = generateReadFromBundleBlock(field, "fragmentSavedInstanceState", "this", true);
            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }

    private CodeBlock generateReadParameterFromBundle(Parameter parameter, String bundleVariableName) {
        TypeName paramType = parameter.getType();
        String varName = parameter.getName();
        String tmpVarName = varName.concat("FromBundle");
        boolean isOptional = parameter.isOptional();
        boolean isNullable = parameter.isNullable();
        boolean isNullableOrOptional = isNullable || isOptional;
        boolean isPrimitive = parameter.getType().isPrimitive();
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (!isPrimitive && !isNullableOrOptional) {
            codeBlock.beginControlFlow("if(!$L.containsKey($S))", bundleVariableName, varName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("%s is not optional.", varName))
                    .endControlFlow();

            generateReadStatement(paramType, codeBlock, bundleVariableName, tmpVarName, varName);
            generateVariableSetValueStatement(codeBlock, parameter, "this", tmpVarName, true);

            codeBlock.beginControlFlow("if($L == null)", tmpVarName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("%s can not be null but null value received from bundle.", varName))
                    .endControlFlow();
        } else {
            codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
            generateReadStatement(paramType, codeBlock, bundleVariableName, tmpVarName, varName);
            generateVariableSetValueStatement(codeBlock, parameter, "this", tmpVarName, true);
            codeBlock.endControlFlow();
        }

        return codeBlock.build();
    }
}