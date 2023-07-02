package com.dslplatform.json

import java.time.Instant

@JsonConverter(target = Instant::class)
object InstantConverter {
    fun read(reader: JsonReader<*>): Instant? {
        return if (reader.wasNull()) null else Instant.parse(reader.readSimpleString())
    }

    fun write(writer: JsonWriter, value: Instant?) {
        if (value == null) {
            writer.writeNull()
        } else {
            writer.writeString(value.toString())
        }
    }
}

@CompiledJson
data class InstantClass(
    val name: String,
    val time: Instant
)