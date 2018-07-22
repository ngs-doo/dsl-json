package com.dslplatform.json.runtime;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

class Java8ParameterNameExtractor implements ParameterNameExtractor {
    @Override
    public String[] extractNames(AccessibleObject ctorOrMethod) {
        final Parameter[] ctorParams = ((Executable) ctorOrMethod).getParameters();
        final String[] names = new String[ctorParams.length];
        for (int i = 0; i < ctorParams.length; i++) {
            if (!ctorParams[i].isNamePresent()) {
                return null;
            }
            names[i] = ctorParams[i].getName();
        }
        return names;
    }
}
