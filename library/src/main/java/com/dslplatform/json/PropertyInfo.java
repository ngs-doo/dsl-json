package com.dslplatform.json;

import java.util.Arrays;

public final class PropertyInfo<T> {
    private final String name;
    private final byte[] quoted;
    public PropertyInfo(String name, byte[] quoted) {
        this.name = name;
        this.quoted = quoted;
    }

    public String getName() {
        return name;
    }
    public byte[] getQuoted() {
        return quoted;
    }
    public boolean isFirstProperty() {
        return quoted[0] == ',';
    }
    public PropertyInfo<T> asFirstProperty(boolean firstProperty) {
        if (firstProperty == isFirstProperty()) return this;
        if (isFirstProperty()) {
            return new PropertyInfo<T>(name, Arrays.copyOfRange(quoted, 1, quoted.length));
        } else {
            byte[] newQuoted = new byte[quoted.length + 1];
            System.arraycopy(quoted, 0, newQuoted, 1, quoted.length);
            newQuoted[0] = JsonWriter.COMMA;
            return new PropertyInfo<T>(name, newQuoted);
        }
    }
}
