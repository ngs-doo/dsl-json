package json

import com.dslplatform.json.*
import com.dslplatform.json.runtime.Settings
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

//annotation processor will run for the specified data class
//if annotation is missing and it's not accessed through some other object with annotation
//class will be analyzed at runtime
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

fun main(args: Array<String>) {
    //include service loader will load up classes created via annotation processor
    val dslJson = DslJson<Any>(Settings.withRuntime<Any>().includeServiceLoader())
    val dc = DataClass(
        language = "Kotlin",
        versions = listOf(170, 171, 172),
        library = "DSL-JSON",
        custom = CustomObject("abc"),
        factory = ObjectFactory.create("xyz"))
    val output = ByteArrayOutputStream()
    dslJson.serialize(dc, output)

    println(output.toString("UTF-8"))

    //val input = "{\"LANG\":\"Kotlin\",\"versions\":[170,171,172],\"library\":\"DSL-JSON\",\"custom\":{\"text\":\"abc\"},\"factory\":{\"text\":\"xyz\"}}".byteInputStream()
    val input = ByteArrayInputStream(output.toByteArray())

    val deser = dslJson.deserialize(DataClass::class.java, input)

    println(deser)

}
