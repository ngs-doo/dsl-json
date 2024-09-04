package com.dslplatform.json;

public interface PropertyAccessor<T> {
    Object getField(T instance, PropertyInfo<T> field);
}
