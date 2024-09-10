package com.dslplatform.json;

import java.util.List;

public class MinimalControls extends JsonControls<ControlInfo> {
    public final static MinimalControls INSTANCE = new MinimalControls();
    private final static ControlInfo NO_CONTROLS = new ControlInfo() {};

    MinimalControls() {
    }

    public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
        return NO_CONTROLS;
    }

    public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo) {
        return classInfo.getPropertyInfos();
    }


    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final Object value, final boolean checkDefaults, final boolean isNotDefaultValue) {
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

    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore) {
    }
}
