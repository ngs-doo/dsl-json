package com.dslplatform.androidkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.dslplatform.json.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalTime
import java.math.BigDecimal
import java.util.*


class MainActivity : AppCompatActivity() {

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
        //val jsonObject: JsonObjectReference?, //object implementing JsonObject manage their own conversion. They must start with '{'
        //val jsonObjects: List<JsonObjectReference>?,
        val time: LocalTime?, //LocalTime is not supported, but with the use of converter it will work
        val times: List<LocalTime?>?, //even containers with unsupported type will be resolved
        @JsonAttribute(converter = FormatDecimal2::class)
        val decimal2: BigDecimal, //custom formatting can be implemented with per property converters
        val intList: ArrayList<Int>, //most collections are supported through runtime converters
        val map: Map<String, Any>, //even unknown stuff can be used. If it fails it will throw SerializationException
        val person: Person? //immutable objects are supported via builder pattern
    )

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

    @CompiledJson(deserializeName = "custom-name")//by default class name will be used for $type attribute
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

    /*data class JsonObjectReference(val x: Int, val s: String) : JsonObject {

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
    }*/

    @JsonConverter(target = LocalTime::class)
    object LocalTimeConverter {
        val JSON_READER = object:JsonReader.ReadObject<LocalTime?> {
            override fun read(reader: JsonReader<*>): LocalTime? {
                if (reader.wasNull()) return null
                return LocalTime.parse(reader.readSimpleString())
            }
        }
        val JSON_WRITER = object:JsonWriter.WriteObject<LocalTime?> {
            override fun write(writer: JsonWriter, value: LocalTime?) {
                if (value == null) {
                    writer.writeNull()
                } else {
                    writer.writeString(value.toString())
                }
            }
        }
    }

    object FormatDecimal2 {
        val JSON_READER = object:JsonReader.ReadObject<BigDecimal> {
            override fun read(reader: JsonReader<*>): BigDecimal {
                return NumberConverter.deserializeDecimal(reader).setScale(2)
            }
        }
        val JSON_WRITER = object:JsonWriter.WriteObject<BigDecimal> {
            override fun write(writer: JsonWriter, value: BigDecimal) {
                NumberConverter.serializeNullable(value.setScale(2), writer)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dslJson = DSL.JSON()

        val concrete = Concrete()
        concrete.x = 11
        concrete.y = 23
        val parent = ParentClass()
        parent.a = 5
        parent.b = 6

        val instance = Model(
            string = "Hello World!",
            number = 42,
            integers = listOf(1, 2, 3),
            decimals = HashSet(listOf(BigDecimal.ONE, BigDecimal.ZERO)),
            uuids = arrayOf(UUID(1L, 2L), UUID(3L, 4L)),
            longs = Vector(listOf(1L, 2L)),
            nested = listOf(Nested(), null),
            inheritance = parent,
            iface = WithCustomCtor(5, 6),
            person = Person("first name", "last name", 35),
            states = Arrays.asList(State.HI, State.LOW),
            //jsonObject = JsonObjectReference(43, "abcd"),
            //jsonObjects = Collections.singletonList(JsonObjectReference(34, "dcba")),
            time = LocalTime.of(12, 15),
            times = listOf(null, LocalTime.of(8, 16)),
            abs = concrete,
            absList = listOf<Abstract?>(concrete, null, concrete),
            decimal2 = BigDecimal.TEN,
            intList = ArrayList(listOf(123, 456)),
            map = mapOf("abc" to 678, "array" to arrayOf(2, 4, 8))
        )

        val tv = findViewById<TextView>(R.id.tvHello)
        try {
            val os = ByteArrayOutputStream()
            //serialize into stream
            dslJson.serialize(instance, os)

            val stream = ByteArrayInputStream(os.toByteArray())
            //deserialized using stream API
            val result = dslJson.deserialize(Model::class.java, stream)
            tv.setText(result.string)
        } catch (ex: IOException) {
            tv.setText(ex.message)
        }
    }
}
