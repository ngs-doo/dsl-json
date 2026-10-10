package com.dslplatform.json.adt;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@JsonConverter(target = JsonValue.class)
public abstract class JsonValueConverter {

    public static JsonValue read(JsonReader reader) throws IOException {
        if (reader.wasNull()) {
            return JsonNull.INSTANCE;
        } else if (reader.wasTrue()) {
            return JsonBoolean.TRUE;
        } else if (reader.wasFalse()) {
            return JsonBoolean.FALSE;
        } else if (reader.last() == '{') {
            reader.getNextToken();
            HashMap<String, JsonValue> elements = new HashMap<>();
            while (reader.last() != '}') {
                String key = reader.readKey();
                assert reader.getNextToken() == ':';
                elements.put(key, read(reader));
                reader.getNextToken();
                if (reader.last() == ',') reader.getNextToken();
            }
            return new JsonObject(elements.entrySet().stream().map(e -> new JsonObject.Entry(e.getKey(), e.getValue())).toArray(JsonObject.Entry[]::new));
        } else if (reader.last() == '[') {
            reader.getNextToken();
            ArrayList<JsonValue> elements = new ArrayList<>();
            while (reader.last() != ']') {
                elements.add(read(reader));
                reader.getNextToken();
                if (reader.last() == ',') reader.getNextToken();
            }
            return new JsonArray(elements.toArray(new JsonValue[0]));
        } else if (reader.last() == '\"') {
            return new JsonString(StringConverter.deserialize(reader));
        } else {
            return new JsonNumber(NumberConverter.deserializeDouble(reader));
        }
    }

    public static void write(JsonWriter writer, JsonValue value) {
        if (value == null || value == JsonNull.INSTANCE) writer.writeNull();
        if (value == JsonBoolean.TRUE) writer.writeAscii("true");
        else if (value == JsonBoolean.FALSE) writer.writeAscii("false");
        else if (value instanceof JsonObject) {
            JsonObject o = (JsonObject) value;
            writer.writeAscii("{");
            boolean first = true;
            for (int i = 0; i < o.value.length; i++) {
                if (!first) writer.writeAscii(",");
                JsonObject.Entry entry = o.value[i];
                writer.writeString(entry.name);
                writer.writeAscii(":");
                write(writer, entry.value);
                first = false;
            }
            writer.writeAscii("}");
        } else if (value instanceof JsonArray) {
            JsonArray a = (JsonArray) value;
            writer.writeAscii("[");
            boolean first = true;
            for (int i = 0; i <a.value.length; i++) {
                if (!first) writer.writeAscii(",");
                write(writer, a.value[i]);
                first = false;
            }
            writer.writeAscii("]");
        } else if (value instanceof JsonString) {
            StringConverter.serialize(((JsonString)value).value, writer);
        } else if (value instanceof JsonNumber) {
            NumberConverter.serialize(((JsonNumber)value).value, writer);
        } else {
            throw new NotSupportedJsonValueTypeException();
        }
    }

    public final static class NotSupportedJsonValueTypeException extends RuntimeException {}
}
