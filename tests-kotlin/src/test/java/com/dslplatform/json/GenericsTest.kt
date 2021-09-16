package com.dslplatform.json

import com.dslplatform.json.runtime.TypeDefinition
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

class GenericsTest {
    private val dslJson = DslJson<Any>()

    @Test
    @Throws(IOException::class)
    fun testSerializeAndDeserializeGeneric() {
        val model = generateModel()
        val type = object : TypeDefinition<GenericModel<Double>>() {}.type
        val os = ByteArrayOutputStream()
        val writer = dslJson.newWriter()
        writer.reset(os)
        dslJson.serialize(writer, type, model)
        writer.flush()
        val result = dslJson.deserialize(type, os.toByteArray(), os.size()) as GenericModel<Double>
        Assertions.assertThat(result).isEqualToComparingFieldByFieldRecursively(model)
    }

    private fun generateModel(): GenericModel<Double> {
        val model = GenericModel<Double>()
        model.items = listOf(1.0, 1.1)
        return model
    }

}