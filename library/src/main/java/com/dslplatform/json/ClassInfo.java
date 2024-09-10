package com.dslplatform.json;

import java.util.List;

public class ClassInfo<C> {
    private final Class<C> type;
    private final CompiledJson.ObjectFormatPolicy objectFormatPolicy;
    private final List<PropertyInfo> propertyInfos;
    private final PropertyAccessor<C> propertyAccessor;

    public ClassInfo(Class<C> type, CompiledJson.ObjectFormatPolicy objectFormatPolicy, List<PropertyInfo> propertyInfos, PropertyAccessor<C> propertyAccessor) {
        this.type = type;
        this.objectFormatPolicy = objectFormatPolicy;
        this.propertyInfos = propertyInfos;
        this.propertyAccessor = propertyAccessor;
    }

    public Class<C> getType() {
        return type;
    }

    public CompiledJson.ObjectFormatPolicy getObjectFormatPolicy() {
        return objectFormatPolicy;
    }

    public List<PropertyInfo> getPropertyInfos() {
        return propertyInfos;
    }

    public PropertyAccessor<C> getPropertyAccessor() {
        return propertyAccessor;
    }
}
