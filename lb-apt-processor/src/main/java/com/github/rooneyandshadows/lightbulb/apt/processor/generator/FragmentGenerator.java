package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.*;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
public class FragmentGenerator extends CodeGenerator {
    private final ClassName ANDROID_R;
    private final List<FragmentMetadata> fragmentMetadataList;

    public FragmentGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        this.ANDROID_R = classNames.androidResources();
        fragmentMetadataList = annotationResultsRegistry.getFragmentDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        fragmentMetadataList.forEach(fragmentMetadata -> {
            ClassName fragmentSuperClassName = getSuperClassName(fragmentMetadata);
            ClassName instrumentedClassName = getInstrumentedClassName(packageNames.getFragmentsPackage(), fragmentMetadata);

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
        List<FieldMetadata> targets = new ArrayList<>();
        targets.addAll(fragmentMetadata.getScreenParameters());
        targets.addAll(fragmentMetadata.getBindViews());
        targets.addAll(fragmentMetadata.getPersistedValues());
        targets.addAll(fragmentMetadata.getViewBindings());
        targets.addAll(fragmentMetadata.getViewModels());

        copyFieldsForSupertypeTransformation(targets, fields, methods);
    }

    private void generateOnCreateMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        boolean hasParameters = fragmentMetadata.hasParameters();
        boolean hasPersistedVars = fragmentMetadata.hasPersistedValues();
        boolean hasViewModels = fragmentMetadata.hasViewModels();

        if (!hasParameters && !hasPersistedVars) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .returns(void.class)
                .addStatement("super.onCreate(savedInstanceState)");

        if (hasViewModels) {
            FragmentMetadata.ViewModel viewModel = fragmentMetadata.getViewModels().get(0);
            String viewModelFieldName = viewModel.getName();
            TypeName viewModelTypeName = classNames.getTypeName(viewModel);

            builder.addStatement("this.$L = new $T(this).get($T.class)", viewModelFieldName, ANDROID_VIEW_MODEL_PROVIDER, viewModelTypeName);
        }

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
                .addStatement("super.onCreateView(inflater,container,savedInstanceState)");

        if (fragmentMetadata.hasViewBindings()) {
            FragmentMetadata.ViewBinding viewBinding = fragmentMetadata.getViewBindings().get(0);
            String viewBindingFieldName = viewBinding.getName();

            builder.addStatement("this.$L = $T.inflate(inflater,$T.layout.$L,container,false)", viewBindingFieldName, ANDROID_DATA_BINDING_UTIL, ANDROID_R, layoutName)
                    .addStatement("this.$L.setLifecycleOwner(getViewLifecycleOwner())", viewBindingFieldName)
                    .addStatement("return this.$L.getRoot()", viewBindingFieldName);
        } else {
            builder.addStatement("$T layout = inflater.inflate($T.layout.$L, null)", ANDROID_VIEW, ANDROID_R, layoutName)
                    .addStatement("return layout");
        }


        destination.add(builder.build());
    }

    private void generateOnViewCreatedMethod(FragmentMetadata fragmentMetadata, List<MethodSpec> destination) {
        if (!fragmentMetadata.hasBindViews()) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_VIEW, "fragmentView")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onViewCreated(fragmentView,savedInstanceState)")
                .returns(void.class);

        fragmentMetadata.getBindViews().forEach(viewBinding -> {
            Field field = Field.from(viewBinding);
            String resourceName = viewBinding.getResourceName();
            String setStatement = field.getValueSetStatement("view.findViewById($T.id.$L)");

            builder.addStatement("$T view = getView()", ANDROID_VIEW);
            builder.addStatement("$T $L = view.findViewById($T.id.$L)", ANDROID_VIEW);
            builder.addStatement(setStatement, ANDROID_R, resourceName);
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
            CodeBlock readCodeBlock = bundleCodeGenerator.generateReadStatement(field, "arguments", !parameter.isOptional(), !parameter.isNullable());

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

        List<FieldMetadata> targets = new ArrayList<>();
        targets.addAll(fragmentMetadata.getScreenParameters());
        targets.addAll(fragmentMetadata.getPersistedValues());

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("saveVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "outState")
                .returns(void.class);

        targets.forEach(fieldMetadata -> {
            Field field = Field.from(fieldMetadata);
            CodeBlock writeStatement = bundleCodeGenerator.generateWriteStatement(field, "outState");

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

        List<FieldMetadata> targets = new ArrayList<>();
        targets.addAll(fragmentMetadata.getScreenParameters());
        targets.addAll(fragmentMetadata.getPersistedValues());

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("restoreVariablesState")
                .addModifiers(PRIVATE)
                .addParameter(ANDROID_BUNDLE, "fragmentSavedInstanceState")
                .returns(void.class);

        targets.forEach(fieldMetadata -> {
            Field field = Field.from(fieldMetadata);
            CodeBlock readStatement = bundleCodeGenerator.generateReadStatement(field, "fragmentSavedInstanceState");

            builder.addCode(readStatement);
        });

        destination.add(builder.build());
    }
}