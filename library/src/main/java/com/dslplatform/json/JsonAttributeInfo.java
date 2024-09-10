package com.dslplatform.json;

import java.util.Arrays;
import java.util.List; /**
 * Information captured from the annotation
 */
public final class JsonAttributeInfo {
    public final String name;
    public final int index;
    public final List<String> alternativeNames;
    public final boolean hasConverter;
    public final JsonAttribute.IncludePolicy includeToMinimal;

    public JsonAttributeInfo(String name,
                             int index, String[] alternativeNames, boolean hasConverter,
                             JsonAttribute.IncludePolicy includeToMinimal) {
        this.name = name;
        this.index = index;
        this.alternativeNames = Arrays.asList(alternativeNames);
        this.hasConverter = hasConverter;
        this.includeToMinimal = includeToMinimal;
    }
}
