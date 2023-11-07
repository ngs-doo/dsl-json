package com.dslplatform.json.adt;

public final class JsonNull implements JsonValue {
    private JsonNull() {}
    public static final JsonNull INSTANCE = new JsonNull();
}
