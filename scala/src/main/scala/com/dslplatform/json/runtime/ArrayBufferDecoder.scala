package com.dslplatform.json
package runtime

import java.lang.reflect.Type

import scala.collection.mutable

final class ArrayBufferDecoder[E](
  manifest: Type,
  decoder: JsonReader.ReadObject[E],
  empty: () => scala.collection.Iterable[E],
  finalize: mutable.ArrayBuffer[E] => scala.collection.Iterable[E]
) extends JsonReader.ReadObject[scala.collection.Iterable[E]] {

  require(manifest ne null, "manifest can't be null")
  require(decoder ne null, "decoder can't be null")
  require(finalize ne null, "finalize can't be null")

  override def read(reader: JsonReader[_]): scala.collection.Iterable[E] = {
    if (reader.last != '[') {
      throw reader.newParseError("Expecting '[' for array start")
    }
    if (reader.getNextToken() == ']') {
      empty()
    } else {
      val buffer = new mutable.ArrayBuffer[E](4)
      buffer += decoder.read(reader)
      while (reader.getNextToken() == ',') {
        reader.getNextToken()
        buffer += decoder.read(reader)
      }
      if (reader.last() != ']') {
        throw reader.newParseError("Expecting ']' for array end")
      }
      finalize(buffer)
    }
  }
}
