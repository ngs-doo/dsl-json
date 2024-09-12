package com.dslplatform.json;

import java.util.List;

public class MinimalControls extends JsonControls<ControlInfo> {
    public final static MinimalControls INSTANCE = new MinimalControls();
    public static final JsonControls.SingletonFactory<MinimalControls> FACTORY = JsonControls.SingletonFactory.of(MinimalControls.INSTANCE);

    MinimalControls() {
    }

    public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
        return ControlInfo.BLANK;
    }

    @Override
    public <C> PropertyWriteControl shouldWriteCommon(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, boolean checkDefaults, boolean isNotDefaultValue) {
        switch (classInfo.getObjectFormatPolicy()) {
            case DEFAULT:
            case EXPLICIT:
            case CONTROLLED:
            case MINIMAL:
                return isNotDefaultValue ||
                        (propertyInfo.getAttribute().includeToMinimal == JsonAttribute.IncludePolicy.ALWAYS)
                        ? PropertyWriteControl.WRITE_NORMALLY
                        : PropertyWriteControl.IGNORED;
            case FULL:
                return PropertyWriteControl.WRITE_NORMALLY;
            default:
                throw new IllegalArgumentException("Unsupported object format policy: " + classInfo.getObjectFormatPolicy());
        }
    }

    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlInfo, PropertyInfo propertyInfo, JsonWriter writer, int positionBefore) {
    }
}
