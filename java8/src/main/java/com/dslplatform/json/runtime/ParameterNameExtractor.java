package com.dslplatform.json.runtime;

import com.dslplatform.json.Nullable;

import java.lang.reflect.AccessibleObject;

interface ParameterNameExtractor {
    /**
     * Extract parameter names for a class constructor or method
     *
     * @param ctorOrMethod should be instance of {@link java.lang.reflect.Constructor} or {@link java.lang.reflect.Method}
     * @return array of names or null if information is not available
     */
    @Nullable
    String[] extractNames(AccessibleObject ctorOrMethod);
}
