package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
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

@SuppressWarnings("SameParameterValue")
public class FragmentGenerator extends CodeGenerator {
    protected final ClassName ANDROID_R;

    public FragmentGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        this.ANDROID_R = ClassNames.androidResources();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbFragmentDescription> fragmentDescriptions = annotationResultsRegistry.getFragmentDescriptions();

        generateFragments(fragmentDescriptions);
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasFragmentDescriptions();
    }

    private void generateFragments(List<LightbulbFragmentDescription> fragmentDescriptions) {
        fragmentDescriptions.forEach(fragmentInfo -> {
            ClassName instrumentedClassName = fragmentInfo.getInstrumentedClassName();
            ClassName superClassName = fragmentInfo.getSuperClassName();
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

            TypeSpec.Builder generatedClass = TypeSpec.classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .superclass(superClassName)
                    .addMethods(methods);
            try {
                JavaFile.builder(instrumentedClassName.packageName(), generatedClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateFields(LightbulbFragmentDescription fragment, List<FieldSpec> fields, List<MethodSpec> methods) {
        List<Field> targets = new ArrayList<>();
        targets.addAll(fragment.getParameters());
        targets.addAll(fragment.getPersistedVariables());
        targets.addAll(fragment.getViewBindings());

        targets.forEach(field -> {
            boolean hasSetter = field.hasSetter();
            boolean hasGetter = field.hasGetter();
            TypeName fieldTypeName = field.getTypeInformation().getTypeName();

            if (!hasGetter || !hasSetter) {
                Modifier fieldAccessModifier = field.accessModifierAtLeast(PROTECTED) ? field.getAccessModifier() : PROTECTED;
                FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, field.getName(), fieldAccessModifier)
                        .build();
                fields.add(fieldSpec);
            }

            if (hasGetter) {
                Modifier access = field.getGetterAccessModifier() == PRIVATE ? PROTECTED : field.getGetterAccessModifier();

                MethodSpec getter = MethodSpec.methodBuilder(field.getGetterName())
                        .returns(fieldTypeName)
                        .addModifiers(access, ABSTRACT)
                        .build();

                methods.add(getter);
            }

            if (hasSetter) {
                Modifier access = field.getSetterAccessModifier() == PRIVATE ? PROTECTED : field.getSetterAccessModifier();

                MethodSpec setter = MethodSpec.methodBuilder(field.getSetterName())
                        .addParameter(fieldTypeName, "value")
                        .addModifiers(access, ABSTRACT)
                        .build();

                methods.add(setter);
            }
        });
    }

    private void generateOnCreateMethod(LightbulbFragmentDescription fragment, List<MethodSpec> destination) {
        boolean hasParameters = !fragment.getParameters().isEmpty();
        boolean hasPersistedVars = !fragment.getPersistedVariables().isEmpty();

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

    private void generateOnCreateViewMethod(LightbulbFragmentDescription fragment, List<MethodSpec> destination) {
        String layoutName = fragment.getLayoutName();

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

    private void generateOnViewCreatedMethod(LightbulbFragmentDescription fragment, List<MethodSpec> destination) {
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

    private void generateOnSaveInstanceStateMethod(LightbulbFragmentDescription fragment, List<MethodSpec> destination) {
        boolean hasParameters = !fragment.getParameters().isEmpty();
        boolean hasPersistedVars = !fragment.getPersistedVariables().isEmpty();

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

    private void generateFragmentParametersMethod(LightbulbFragmentDescription fragment, List<MethodSpec> destination) {
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

    private void generateSaveVariablesMethod(LightbulbFragmentDescription fragmentInfo, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ClassNames.ANDROID_BUNDLE, "outState")
                .returns(void.class);

        List<Field> fieldsToSave = new ArrayList<>();
        fieldsToSave.addAll(fragmentInfo.getParameters());
        fieldsToSave.addAll(fragmentInfo.getPersistedVariables());

        if (fieldsToSave.isEmpty()) {
            return;
        }

        fieldsToSave.forEach(field -> {
            CodeBlock writeStatement = generateWriteFieldIntoBundleBlock(field, "outState");
            builder.addCode(writeStatement);
        });

        destination.add(builder.build());
    }

    private void generateRestoreVariablesMethod(LightbulbFragmentDescription fragmentInfo, List<MethodSpec> destination) {
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ClassNames.ANDROID_BUNDLE, "fragmentSavedInstanceState")
                .returns(void.class);

        if (fragmentInfo.getParameters().isEmpty() && fragmentInfo.getPersistedVariables().isEmpty()) {
            return;
        }

        List<Field> fieldsToRestore = new ArrayList<>();
        fieldsToRestore.addAll(fragmentInfo.getParameters());
        fieldsToRestore.addAll(fragmentInfo.getPersistedVariables());

        fieldsToRestore.forEach(field -> {
            CodeBlock readStatement = generateReadFromBundleBlock(field, "fragmentSavedInstanceState");
            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }

    private CodeBlock generateReadParameterFromBundle(Parameter parameter, String bundleVariableName) {
        TypeInformation type = parameter.getTypeInformation();
        String varName = parameter.getName();
        String tmpVarName = varName.concat("FromBundle");
        boolean isOptional = parameter.isOptional();
        boolean isNullable = parameter.isNullable();
        boolean isNullableOrOptional = isNullable || isOptional;
        boolean isPrimitive = type.isPrimitive();
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (!isPrimitive && !isNullableOrOptional) {
            codeBlock.beginControlFlow("if(!$L.containsKey($S))", bundleVariableName, varName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("%s is not optional.", varName))
                    .endControlFlow();

            generateReadStatement(codeBlock, type, bundleVariableName, tmpVarName, varName);
            generateFieldSetValueStatement(codeBlock, parameter, tmpVarName, true);

            codeBlock.beginControlFlow("if($L == null)", tmpVarName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("%s can not be null but null value received from bundle.", varName))
                    .endControlFlow();
        } else {
            codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
            generateReadStatement(codeBlock, type, bundleVariableName, tmpVarName, varName);
            generateFieldSetValueStatement(codeBlock, parameter, tmpVarName, true);
            codeBlock.endControlFlow();
        }

        return codeBlock.build();
    }

    private CodeBlock generateWriteFieldIntoBundleBlock(Field field, String bundleVariableName) {
        TypeInformation parameterTypeInfo = field.getTypeInformation();
        String fieldName = field.getName();
        CodeBlock.Builder writeIntoBundleCodeBlock = CodeBlock.builder();
        String accessorStatement = (field.hasGetter()) ? field.getGetterName().concat("()") : fieldName;
        String variableAccessor = String.format("this.%s", accessorStatement);

        BundleCodeGenerator.generateWriteStatement(writeIntoBundleCodeBlock, parameterTypeInfo, bundleVariableName, variableAccessor, fieldName);

        return writeIntoBundleCodeBlock.build();
    }

    private CodeBlock generateReadFromBundleBlock(Field variable, String bundleVariableName) {
        String varName = variable.getName();
        String tmpVarName = String.format("%s%s", varName, "FromBundle");

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
        generateReadStatement(codeBlock, variable.getTypeInformation(), bundleVariableName, tmpVarName, varName);
        generateFieldSetValueStatement(codeBlock, variable, tmpVarName, true);
        codeBlock.endControlFlow();

        return codeBlock.build();
    }
}