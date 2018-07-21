package com.dslplatform.json.runtime;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

import java.lang.reflect.AccessibleObject;

class ParanamerParameterNameExtractor implements ParameterNameExtractor {
    private final Paranamer paranamer = new AdaptiveParanamer();

    @Override
    public String[] extractNames(AccessibleObject ctorOrMethod) {
        String[] names = paranamer.lookupParameterNames(ctorOrMethod, false);
        return names == Paranamer.EMPTY_NAMES ? null : names;
    }
}