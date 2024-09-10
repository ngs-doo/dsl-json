package com.dslplatform.json;

import java.util.List;

public abstract class JsonControls<T extends ControlInfo> {

    protected JsonControls() {
    }

    public abstract <C> T controlledInfo(C instance, ClassInfo<C> classInfo);

    public abstract <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, T controlledInfo);


    public abstract <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final Object value, final boolean checkDefaults, final boolean isNotDefaultValue);

    public abstract <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore);
}