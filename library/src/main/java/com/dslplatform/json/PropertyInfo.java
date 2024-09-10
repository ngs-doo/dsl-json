package com.dslplatform.json;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PropertyInfo {
    private final String name;
    private final byte[] quotedNameAndColon;

    public JsonAttributeInfo getAttribute() {
        return attribute;
    }

    private final JsonAttributeInfo attribute;

    public PropertyInfo(String name, JsonAttributeInfo attribute) {
        this.name = name;
        this.quotedNameAndColon = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
        this.attribute = attribute;
    }

    public String getName() {
        return name;
    }
    public byte[] getQuotedNameAndColon() {
        return quotedNameAndColon;
    }

}
