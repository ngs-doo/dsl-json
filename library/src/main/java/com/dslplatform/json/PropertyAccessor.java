package com.dslplatform.json;

public abstract class PropertyAccessor<T> {
    private final Class<T> clazz;
    public PropertyAccessor(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract Object getField(T instance, PropertyInfo field);
}
