package com.dslplatform.androidkotlin

import com.dslplatform.json.*
import com.dslplatform.json.runtime.MapAnalyzer
import com.dslplatform.json.runtime.Settings
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

object DSL {
    private var json: DslJson<Any>? = null
    fun JSON(): DslJson<Any> {
        var tmp = json
        if (tmp != null) return tmp
        //during initialization ServiceLoader.load should pick up services registered into META-INF/services
        //this doesn't really work on Android so DslJson will fallback to default generated class name
        //"dsl_json_Annotation_Processor_External_Serialization" and try to initialize it manually
        tmp = DslJson(Settings.withRuntime<Any>().includeServiceLoader().allowArrayFormat(true))
        json = tmp
        return tmp
    }
}

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE) //ignore unknown properties (default for objects). to disallow unknown properties in JSON set it to FAIL which will result in exception instead
data class Model ( //data classes are supported the same way as immutable objects in Java
    val string: String,//not null annotations indicate that field can't be null
    val integers: List<Int>?,
    @JsonAttribute(name = "guids") //use alternative name in JSON
    val uuids: Array<UUID>?,
    val decimals: Set<BigDecimal>?,
    val longs: Vector<Long>,
    @JsonAttribute(hashMatch = false) // exact name match can be forced, otherwise hash value will be used for matching
    val number: Int,
    @JsonAttribute(alternativeNames = arrayOf("old_nested", "old_nested2")) //several JSON attribute names can be deserialized into this field
    val nested: List<Nested?>,
    @JsonAttribute(typeSignature = CompiledJson.TypeSignature.EXCLUDE) //$type attribute can be excluded from resulting JSON
    val abs: Abstract?,//abstract classes or interfaces can be used which will also include $type attribute in JSON by default
    val absList: List<Abstract?>,
    val iface: Interface?,//interfaces without deserializedAs will also include $type attribute in JSON by default
    val inheritance: ParentClass?,
    @JsonAttribute(mandatory = true)// mandatory adds check if property exist in JSON and will serialize it even in omit-defaults mode
    val states: List<State>?,
    val jsonObject: JsonObjectReference?, //object implementing JsonObject manage their own conversion. They must start with '{'
    val jsonObjects: List<JsonObjectReference>?,
    val time: LocalTime?, //LocalTime is not supported, but with the use of converter it will work
    val times: List<LocalTime?>?, //even containers with unsupported type will be resolved
    @JsonAttribute(converter = FormatDecimal2::class)
    val decimal2: BigDecimal, //custom formatting can be implemented with per property converters
    val intList: ArrayList<Int>, //most collections are supported through runtime converters
    //since this signature has an unknown part (Object), it must be whitelisted
    //This can be done via appropriate converter, by registering @JsonConverter for the specified type
    //or by enabling support for unknown types in the annotation processor
    @JsonAttribute(converter = MapAnalyzer.Runtime::class)
    val map: Map<String, Any>,
    val person: Person? //immutable objects are supported via builder pattern
)

@CompiledJson(formats = arrayOf(CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT))
data class Person(val firstName: String, val lastName: String, val age: Int)

//explicitly referenced classes don't require @CompiledJson annotation
class Nested {
    var x: Long = 0
    var y: Double = 0.toDouble()
    var z: Float = 0.toFloat()
}

@CompiledJson(deserializeAs = Concrete::class)//without deserializeAs deserializing Abstract would fails since it doesn't contain a $type due to it's exclusion in the above configuration
abstract class Abstract {
    var x: Int = 0
}

//since this class is not explicitly referenced, but it's an extension of the abstract class used as a property
//it needs to be decorated with annotation
@CompiledJson
class Concrete : Abstract() {
    var y: Long = 0
}

interface Interface {
    fun x(v: Int)
    fun x(): Int
}

@CompiledJson(name = "custom-name")//by default class name will be used for $type attribute
class WithCustomCtor : Interface {
    private var x: Int = 0
    var y: Int = 0

    constructor(x: Int) {
        this.x = x
        this.y = x
    }

    @CompiledJson
    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    override fun x(v: Int) {
        x = v
    }

    override fun x(): Int {
        return x
    }
}

open class BaseClass {
    var a: Int = 0
}

class ParentClass : BaseClass() {
    var b: Long = 0
}

enum class State private constructor(private val value: Int) {
    LOW(0),
    MID(1),
    HI(2)
}

data class JsonObjectReference(val x: Int, val s: String) : JsonObject {

    override fun serialize(writer: JsonWriter, minimal: Boolean) {
        writer.writeAscii("{\"x\":")
        NumberConverter.serialize(x, writer)
        writer.writeAscii(",\"s\":")
        StringConverter.serialize(s, writer)
        writer.writeAscii("}")
    }

    companion object {

        val JSON_READER = object:JsonReader.ReadJsonObject<JsonObjectReference> {
            override fun deserialize(reader: JsonReader<*>): JsonObjectReference {
                reader.fillName()//"x"
                reader.getNextToken()//start number
                val x = NumberConverter.deserializeInt(reader)
                reader.getNextToken()//,
                reader.getNextToken()//start name
                reader.fillName()//"s"
                reader.getNextToken()//start string
                val s = StringConverter.deserialize(reader)
                reader.getNextToken()//}
                return JsonObjectReference(x, s)
            }
        }
    }
}

@JsonConverter(target = LocalTime::class)
object LocalTimeConverter {
    fun read(reader: JsonReader<*>): LocalTime? {
        if (reader.wasNull()) return null
        return LocalTime.parse(reader.readSimpleString())
    }
    fun write(writer: JsonWriter, value: LocalTime?) {
        if (value == null) {
            writer.writeNull()
        } else {
            writer.writeString(value.toString())
        }
    }
}

object FormatDecimal2 {
    fun read(reader: JsonReader<*>): BigDecimal {
        return NumberConverter.deserializeDecimal(reader).setScale(2)
    }

    fun write(writer: JsonWriter, value: BigDecimal) {
        NumberConverter.serializeNullable(value.setScale(2), writer)
    }
}