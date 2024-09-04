package com.dslplatform.json;

public final class PropertyInfo<T> {
    private final String name;
    private final byte[] quoted;
    public PropertyInfo(String name, byte[] quoted) {
        this.name = name;
        this.quoted = quoted;
    }
    public void writeQuoted(com.dslplatform.json.JsonWriter writer) {
        writer.writeAscii(quoted);
    }

    public String getName() {
        return name;
    }
}
