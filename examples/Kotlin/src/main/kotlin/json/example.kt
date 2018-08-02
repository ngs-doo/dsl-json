package json

import com.dslplatform.json.CompiledJson
import com.dslplatform.json.DslJson
import com.dslplatform.json.JsonAttribute
import com.dslplatform.json.runtime.Settings
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

//annotation processor will run for the specified data class
//if annotation is missing and it's not accessed through some other object with annotation
//class will be analyzed at runtime
@CompiledJson
data class DataClass(
        @JsonAttribute(name = "lang") val language: String,
        val versions: List<Int>,
        val library: String
)

fun main(args: Array<String>) {
    //include service loader will load up classes created via annotation processor
    val dslJson = DslJson<Any>(Settings.withRuntime<Any>().includeServiceLoader())
    val dc = DataClass(language = "Kotlin", versions = listOf(170, 171, 172), library = "DSL-JSON")
    val output = ByteArrayOutputStream()
    dslJson.serialize(dc, output)

    println(output.toString("UTF-8"))

    val input = ByteArrayInputStream(output.toByteArray())

    val deser = dslJson.deserialize(DataClass::class.java, input)

    println(deser)

}
