package com.dslplatform.json
package runtime

import java.lang.reflect.Type

final class ScalaMapImmutableDecoder[K, V](
  manifest: Type,
  keyDecoder: JsonReader.ReadObject[K],
  valueDecoder: JsonReader.ReadObject[V]
) extends JsonReader.ReadObject[Map[K, V]] {
  require(manifest ne null, "manifest can't be null")
  require(keyDecoder ne null, "keyDecoder can't be null")
  require(valueDecoder ne null, "valueDecoder can't be null")

  override def read(reader: JsonReader[_]): Map[K, V] = {
    if (reader.last != '{') throw reader.newParseError("Expecting '{' for map start")
    val builder = Map.newBuilder[K, V]
    if (reader.getNextToken() == '}') builder.result()
    else {
      var key = keyDecoder.read(reader)
      if (key == null) {
        throw reader.newParseErrorFormat(
          "Null value detected for key element of map",
          0,
          "Null value detected for key element of %s", manifest.getTypeName)
      }
      if (reader.getNextToken() != ':') throw reader.newParseError("Expecting ':' after attribute name")
      reader.getNextToken()
      var value = valueDecoder.read(reader)
      builder += key -> value
      while (reader.getNextToken() == ',') {
        reader.getNextToken()
        key = keyDecoder.read(reader)
        if (key == null) {
          throw reader.newParseErrorFormat(
            "Null value detected for key element of map",
            0,
            "Null value detected for key element of %s", manifest.getTypeName)
        }
        if (reader.getNextToken() != ':') throw reader.newParseError("Expecting ':' after attribute name")
        reader.getNextToken()
        value = valueDecoder.read(reader)
        builder += key -> value
      }
      if (reader.last != '}') throw reader.newParseError("Expecting '}' for map end")
      builder.result()
    }
  }
}
