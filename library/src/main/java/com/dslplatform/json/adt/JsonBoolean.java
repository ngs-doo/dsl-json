package com.dslplatform.json.adt;

// uncomment for java 21
// public record JsonBoolean(boolean value) implements JsonValue {
//     public static final JsonBoolean TRUE = new JsonBoolean(true);
//     public static final JsonBoolean FALSE = new JsonBoolean(false);
// }

public final class JsonBoolean implements JsonValue {
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    public final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }
}
