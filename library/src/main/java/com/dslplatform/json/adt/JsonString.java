package com.dslplatform.json.adt;

// uncomment for java 21
// public record JsonString(String value) implements JsonValue { }

public final class JsonString implements JsonValue {
    public final String value;

    public JsonString(String value) {
        this.value = value;
    }
}
