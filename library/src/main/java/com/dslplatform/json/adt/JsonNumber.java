package com.dslplatform.json.adt;

// uncomment for java 21
// public record JsonNumber(double value) implements JsonValue { }

public final class JsonNumber implements JsonValue {
    public final double value;

    public JsonNumber(double value) {
        this.value = value;
    }
}
