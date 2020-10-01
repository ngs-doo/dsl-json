package com.dslplatform.json.runtime;

import com.dslplatform.json.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

class Java8ParameterNameExtractor implements ParameterNameExtractor {
    @Nullable
    @Override
    public String[] extractNames(AccessibleObject ctorOrMethod) {
        final Parameter[] params = ((Executable) ctorOrMethod).getParameters();
        final String[] names = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            if (!params[i].isNamePresent()) {
                return null;
            }
            names[i] = params[i].getName();
        }
        return names;
    }
}
