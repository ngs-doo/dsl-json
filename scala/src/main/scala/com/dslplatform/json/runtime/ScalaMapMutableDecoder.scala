package com.dslplatform.json
package runtime

import java.io.IOException
import java.lang.reflect.Type

import scala.collection.mutable

final class ScalaMapMutableDecoder[K, V](
  manifest: Type,
  keyDecoder: JsonReader.ReadObject[K],
  valueDecoder: JsonReader.ReadObject[V],
  finalize: mutable.LinkedHashMap[K, V] => scala.collection.Map[K, V]
) extends JsonReader.ReadObject[scala.collection.Map[K, V]] {
  require(manifest ne null, "manifest can't be null")
  require(keyDecoder ne null, "keyDecoder can't be null")
  require(valueDecoder ne null, "valueDecoder can't be null")
  require(finalize ne null, "finalize can't be null")

  override def read(reader: JsonReader[_]): scala.collection.Map[K, V] = {
    if (reader.last != '{') throw new IOException(s"Expecting '{' at position ${reader.positionInStream}. Found ${reader.last.toChar}")
    val result = new mutable.LinkedHashMap[K, V]
    if (reader.getNextToken() == '}') finalize(result)
    else {
      var key = keyDecoder.read(reader)
      if (key == null) throw new IOException(s"Null value detected for key element of $manifest at position ${reader.positionInStream}")
      if (reader.getNextToken() != ':') throw new IOException(s"Expecting ':' at position ${reader.positionInStream}. Found ${reader.last.toChar}")
      reader.getNextToken()
      var value = valueDecoder.read(reader)
      result += key -> value
      while (reader.getNextToken() == ',') {
        reader.getNextToken()
        key = keyDecoder.read(reader)
        if (key == null) throw new IOException(s"Null value detected for key element of $manifest at position ${reader.positionInStream}")
        if (reader.getNextToken() != ':') throw new IOException(s"Expecting ':' at position ${reader.positionInStream}. Found ${reader.last.toChar}")
        reader.getNextToken()
        value = valueDecoder.read(reader)
        result += key -> value
      }
      if (reader.last != '}') throw new IOException(s"Expecting '}' at position ${reader.positionInStream}. Found ${reader.last.toChar}")
      finalize(result)
    }
  }
}
