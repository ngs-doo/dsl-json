package com.dslplatform.json.adt;

// uncomment for java 21
// public record JsonArray(DslJsonValue[] value) implements JsonValue { }

public final class JsonArray implements JsonValue {
    public final JsonValue[] value;

    public JsonArray(JsonValue[] value) {
        this.value = value;
    }
}
