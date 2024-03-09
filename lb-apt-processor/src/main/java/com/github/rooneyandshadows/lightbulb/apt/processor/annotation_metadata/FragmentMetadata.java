package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Collectors;

public final class FragmentMetadata extends ClassMetadata {
    private final String layoutName;
    private final String screenName;
    private final String screenGroupName;
    private final ViewModel viewModel;
    private final ViewBinding viewBinding;
    private final List<ScreenParameter> screenParameters;
    private final List<StatePersisted> persistedValues;
    private final List<BindView> bindViews;

    public FragmentMetadata(
            TypeElement element,
            String layoutName,
            String screenName,
            String screenGroupName,
            ViewModel viewModels,
            ViewBinding viewBinding,
            List<ScreenParameter> screenParameters,
            List<StatePersisted> persistedValues,
            List<BindView> bindViews
    ) {
        super(element);
        this.layoutName = layoutName;
        this.screenName = screenName;
        this.screenGroupName = screenGroupName;
        this.viewModel = viewModels;
        this.viewBinding = viewBinding;
        this.screenParameters = screenParameters;
        this.persistedValues = persistedValues;
        this.bindViews = bindViews;

    }

    public boolean isScreen() {
        return !(screenName == null || screenName.isEmpty());
    }

    public String getLayoutName() {
        return layoutName;
    }

    public String getScreenName() {
        return screenName;
    }

    public List<ScreenParameter> getScreenParameters() {
        return screenParameters;
    }

    public List<StatePersisted> getPersistedValues() {
        return persistedValues;
    }

    public List<BindView> getBindViews() {
        return bindViews;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public ViewBinding getViewBinding() {
        return viewBinding;
    }

    public boolean hasBindViews() {
        return !bindViews.isEmpty();
    }

    public boolean hasPersistedValues() {
        return !persistedValues.isEmpty();
    }

    public boolean hasParameters() {
        return !screenParameters.isEmpty();
    }

    public boolean hasOptionalParameters() {
        return screenParameters.stream().anyMatch(ScreenParameter::isOptional);
    }

    public boolean hasViewBinding() {
        return viewBinding != null;
    }

    public boolean hasViewModel() {
        return viewModel != null;
    }

    public List<ScreenParameter> getScreenParameters(boolean includeOptional) {
        return includeOptional ? screenParameters : screenParameters.stream()
                .filter(paramInfo -> !paramInfo.isOptional())
                .collect(Collectors.toList());
    }

    public String getScreenGroupName() {
        if ((screenGroupName == null || screenGroupName.isEmpty())) {
            return "Common";
        }
        return screenGroupName;
    }

    public static final class ScreenParameter extends FieldMetadata {
        private final boolean optional;

        public ScreenParameter(VariableElement element, boolean optional) {
            super(element);
            this.optional = optional;
        }

        public boolean isOptional() {
            return optional;
        }
    }

    public static final class StatePersisted extends FieldMetadata {
        public StatePersisted(VariableElement element) {
            super(element);
        }
    }

    public static final class BindView extends FieldMetadata {
        private final String resourceName;

        public BindView(VariableElement element, String resourceName) {
            super(element);
            this.resourceName = resourceName;
        }

        public String getResourceName() {
            return resourceName;
        }
    }

    public static final class ViewBinding extends FieldMetadata {
        private final String layoutName;

        public ViewBinding(VariableElement element, String layoutName) {
            super(element);
            this.layoutName = layoutName;
        }

        public String getLayoutName() {
            return layoutName;
        }
    }

    public static final class ViewModel extends FieldMetadata {
        public ViewModel(VariableElement element) {
            super(element);
        }
    }
}