package com.dslplatform.json.runtime;

import java.lang.reflect.AccessibleObject;
import java.util.List;

class CompositeParameterNameExtractor implements ParameterNameExtractor {
    private final ParameterNameExtractor[] extractors;

    CompositeParameterNameExtractor(List<ParameterNameExtractor> extractors) {
        this.extractors = extractors.toArray(new ParameterNameExtractor[0]);
    }

    @Override
    public String[] extractNames(AccessibleObject ctorOrMethod) {
        for (ParameterNameExtractor extractor : extractors) {
            String[] names = extractor.extractNames(ctorOrMethod);
            if (names != null) return names;
        }
        return null;
    }
}
