package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.BundleCodeGenerator.generateReadStatement;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.BundleCodeGenerator.generateWriteStatement;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
public class FragmentGenerator extends CodeGenerator {
    private final ClassName ANDROID_R;
    private final List<FragmentMetadata> fragmentMetadataList;

    public FragmentGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, annotationResultsRegistry);
        this.ANDROID_R = androidResources();
        fragmentMetadataList = annotationResultsRegistry.getFragmentDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        fragmentMetadataList.forEach(fragmentMetadata -> {
            ClassName fragmentSuperClassName = fragmentMetadata.getSuperClassName();
            ClassName instrumentedClassName = fragmentMetadata.getInstrumentedClassName();
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(fragmentMetadata, fields, methods);
            generateOnCreateMethod(fragmentMetadata, methods);
            generateOnCreateViewMethod(fragmentMetadata, methods);
            generateOnViewCreatedMethod(fragmentMetadata, methods);
            generateOnSaveInstanceStateMethod(fragmentMetadata, methods);
            generateFragmentParametersMethod(fragmentMetadata, methods);
            generateSaveVariablesMethod(fragmentMetadata, methods);
            generateRestoreVariablesMethod(fragmentMetadata, methods);

            TypeSpec.Builder fragmentClassBuilder = TypeSpec.classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .addMethods(methods);

            if (fragmentSuperClassName != null) {
                fragmentClassBuilder.superclass(fragmentSuperClassName);
            }

            writeClassFile(instrumentedClassName.packageName(), fragmentClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasFragmentDescriptions();
    }

    private void generateFields(FragmentMetadata fragmentMetadata, List<FieldSpec> fields, List<MethodSpec> methods) {
        List<Field> paramFields = fragmentMetadata.getScreenParameters().stream().map(Field::from).toList();
        List<Field> viewBindingFields = fragmentMetadata.getViewBindings().stream().map(Field::from).toList();
        List<Field> persistedValueFields = fragmentMetadata.getPersistedValues().stream().map(Field::from).toList();
        List<Field> targets = new ArrayList<>();
        targets.addAll(paramFields);
        targets.addAll(viewBindingFields);
        targets.addAll(persistedValueFields);

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

    private void generateOnCreateMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();
        boolean hasPersistedVars = fragmentMetadata.hasPersistedValues();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (!hasParameters) {
            builder.beginControlFlow("if(savedInstanceState != null)")
                    .addStatement("restoreVariablesState(savedInstanceState)")
                    .endControlFlow();
        } else {
            builder.beginControlFlow("if(savedInstanceState == null)")
                    .addStatement("$T arguments = getArguments()", ANDROID_BUNDLE)
                    .addStatement("generateParameters(arguments)")
                    .nextControlFlow("else")
                    .addStatement("restoreVariablesState(savedInstanceState)")
                    .endControlFlow();
        }

        destination.add(builder.build());
    }

    private void generateOnCreateViewMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        String layoutName = fragmentMetadata.getLayoutName();

        if (layoutName == null || layoutName.isBlank()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateView")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_LAYOUT_INFLATER, "inflater")
                .addParameter(ANDROID_VIEW_GROUP, "container")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .returns(ANDROID_VIEW)
                .addStatement("super.onCreateView(inflater,container,savedInstanceState)")
                .addStatement("$T layout = inflater.inflate($T.layout.$L, null)", ANDROID_VIEW, ANDROID_R, layoutName)
                .addStatement("return layout");

        destination.add(builder.build());
    }

    private void generateOnViewCreatedMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        if (!fragmentMetadata.hasViewBindings()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_VIEW, "fragmentView")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onViewCreated(fragmentView,savedInstanceState)")
                .returns(void.class);

        fragmentMetadata.getViewBindings().forEach(viewBinding -> {
            Field field = Field.from(viewBinding);
            String resourceName = viewBinding.name();

            builder.addStatement("$T view = getView()", ANDROID_VIEW);

            if (field.hasSetter()) {
                String statement = "$L(view.findViewById($T.id.$L))";
                builder.addStatement(statement, field.getSetterName(), ANDROID_R, resourceName);
            } else {
                String statement = "$L = view.findViewById($T.id.$L)";
                builder.addStatement(statement, field.getName(), ANDROID_R, resourceName);
            }
        });

        destination.add(builder.build());
    }

    private void generateOnSaveInstanceStateMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();
        boolean hasPersistedVars = fragmentMetadata.hasPersistedValues();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onSaveInstanceState")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_BUNDLE, "outState")
                .addStatement("super.onSaveInstanceState(outState)")
                .addStatement("saveVariablesState(outState)")
                .returns(void.class);

        destination.add(builder.build());
    }

    private void generateFragmentParametersMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();

        if (!hasParameters) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("generateParameters")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "arguments")
                .returns(void.class);

        fragmentMetadata.getScreenParameters().forEach(parameter -> {
            Field field = Field.from(parameter);
            CodeBlock readCodeBlock = generateReadStatement(field, "arguments", !parameter.optional(), !field.isNullable());

            builder.addCode(readCodeBlock);
        });

        destination.add(builder.build());
    }

    private void generateSaveVariablesMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();
        boolean hasPersistedValues = fragmentMetadata.hasPersistedValues();

        if (!hasParameters && !hasPersistedValues) {
            return;
        }

        List<Field> paramFields = fragmentMetadata.getScreenParameters().stream().map(Field::from).toList();
        List<Field> persistedValueFields = fragmentMetadata.getPersistedValues().stream().map(Field::from).toList();
        List<Field> fieldsToSave = new ArrayList<>();
        fieldsToSave.addAll(paramFields);
        fieldsToSave.addAll(persistedValueFields);

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "outState")
                .returns(void.class);

        fieldsToSave.forEach(field -> {
            CodeBlock writeStatement = generateWriteStatement(field, "outState");

            builder.addCode(writeStatement);
        });

        destination.add(builder.build());
    }

    private void generateRestoreVariablesMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();
        boolean hasPersistedValues = fragmentMetadata.hasPersistedValues();

        if (!hasParameters && !hasPersistedValues) {
            return;
        }

        List<Field> paramFields = fragmentMetadata.getScreenParameters().stream().map(Field::from).toList();
        List<Field> persistedValueFields = fragmentMetadata.getPersistedValues().stream().map(Field::from).toList();
        List<Field> fieldsToRestore = new ArrayList<>();
        fieldsToRestore.addAll(paramFields);
        fieldsToRestore.addAll(persistedValueFields);

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "fragmentSavedInstanceState")
                .returns(void.class);

        fieldsToRestore.forEach(field -> {
            CodeBlock readStatement = generateReadStatement(field, "fragmentSavedInstanceState");
            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }
}