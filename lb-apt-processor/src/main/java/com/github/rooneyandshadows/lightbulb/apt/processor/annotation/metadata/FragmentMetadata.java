package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.element.Element;
import java.util.List;

public final class FragmentMetadata extends BaseMetadata {
    private final String layoutName;
    private final String screenName;
    private final String screenGroupName;
    private final List<Parameter> screenParameters;
    private final List<StatePersisted> persistedValues;
    private final List<ViewBinding> viewBindings;

    public FragmentMetadata(Element element, String layoutName, String screenName, String screenGroupName, List<Parameter> screenParameters, List<StatePersisted> persistedValues, List<ViewBinding> viewBindings) {
        super(element);
        this.layoutName = layoutName;
        this.screenName = screenName;
        this.screenGroupName = screenGroupName;
        this.screenParameters = screenParameters;
        this.persistedValues = persistedValues;
        this.viewBindings = viewBindings;
    }

    public boolean isScreen() {
        return !(screenName == null || screenName.isEmpty());
    }

    public String getScreenGroupName() {
        if ((screenGroupName == null || screenGroupName.isEmpty())) {
            return "Common";
        }
        return screenGroupName;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public String getScreenName() {
        return screenName;
    }

    public List<Parameter> getScreenParameters() {
        return screenParameters;
    }

    public List<StatePersisted> getPersistedValues() {
        return persistedValues;
    }

    public List<ViewBinding> getViewBindings() {
        return viewBindings;
    }

    public record Parameter(Element element, boolean optional) {
    }

    public record StatePersisted(Element element) {
    }

    public record ViewBinding(Element element, String name) {
    }
}