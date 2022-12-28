package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class GenericSelfReference<T> implements Comparable<GenericSelfReference<T>> {

    final T genericField;

    public GenericSelfReference(T genericField) {
        this.genericField = genericField;
    }

    public final T getGenericField() {
        return genericField;
    }

    @Override
    public int compareTo(GenericSelfReference<T> other) {
        return 1;
    }
}