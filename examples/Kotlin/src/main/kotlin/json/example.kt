package json

import com.dslplatform.json.DslJson
import com.dslplatform.json.runtime.Settings
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

data class DataClass(val language: String, val versions: List<Int>, val library: String)

fun main(args: Array<String>) {
    val dslJson = DslJson<Any>(Settings.withRuntime())
    val dc = DataClass(language = "Kotlin", versions = listOf(170, 171, 172), library = "DSL-JSON")
    val output = ByteArrayOutputStream()
    dslJson.serialize(dc, output)

    println(output.toString("UTF-8"))

    val input = ByteArrayInputStream(output.toByteArray())

    val deser = dslJson.deserialize(DataClass::class.java, input)

    println(deser)

}
