package com.dslplatform.json

@CompiledJson
data class DataClass(
    @JsonAttribute(name = "lang", alternativeNames = ["Lang", "LANG"]) val language: String,
    val versions: List<Int>,
    val library: String,
    val custom: CustomObject,
    val factory: ObjectFactory
)

data class CustomObject(val text: String) : JsonObject {
    override fun serialize(writer: JsonWriter, minimal: Boolean) {
        writer.writeAscii("{\"text\":")
        StringConverter.serialize(text, writer)
        writer.writeAscii("}")
    }
    companion object {
        val JSON_READER = object: JsonReader.ReadJsonObject<CustomObject?> {
            override fun deserialize(reader: JsonReader<Any>): CustomObject? {
                reader.fillName()
                reader.nextToken
                val name = reader.readString()
                reader.endObject()
                return CustomObject(name)
            }
        }
    }
}

class ObjectFactory(val text: String) {
    companion object {
        @CompiledJson
        fun create(text: String): ObjectFactory {
            return ObjectFactory(text)
        }
    }
}
