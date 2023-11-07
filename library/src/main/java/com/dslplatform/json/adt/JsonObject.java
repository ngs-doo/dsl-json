package com.dslplatform.json.adt;

// uncomment for java 21
// public record JsonObject(Entry[] value) implements JsonValue {
//     public record Entry(String name, JsonValue value) {}
// }

public final class JsonObject implements JsonValue {
    public final Entry[] value;

    public JsonObject(Entry[] value) {
        this.value = value;
    }

    public static class Entry {
        public final String name;
        public final JsonValue value;

        public Entry(String name, JsonValue value) {
            this.name = name;
            this.value = value;
        }
    }
}
