package com.dslplatform.json
package runtime

import scala.collection.Iterable

final class IterableEncoder[E](
  json: DslJson[_],
  encoder: Option[JsonWriter.WriteObject[E]]) extends JsonWriter.WriteObject[Iterable[E]] {

  require(manifest ne null, "manifest can't be null")
  require(encoder ne null, "encoder can't be null")

  private val EMPTY = Array[Byte]('[', ']')

  override def write(writer: JsonWriter, value: Iterable[E]): Unit = {
    //TODO: remove null check
    if (value == null) writer.writeNull()
    else if (value.isEmpty) writer.writeAscii(EMPTY)
    else if (encoder.isDefined) {
      writer.writeByte(JsonWriter.ARRAY_START)
      val enc = encoder.get
      value match {
        case iseq: IndexedSeq[E] =>
          enc.write(writer, iseq.head)
          var i = 1
          val size = iseq.size
          while (i < size) {
            writer.writeByte(JsonWriter.COMMA)
            enc.write(writer, iseq(i))
            i += 1
          }
        case _ =>
          val iter = value.iterator
          enc.write(writer, iter.next())
          while (iter.hasNext) {
            val v = iter.next()
            writer.writeByte(JsonWriter.COMMA)
            enc.write(writer, v)
          }
      }
      writer.writeByte(JsonWriter.ARRAY_END)
    } else {
      var pastFirst = false
      writer.writeByte(JsonWriter.ARRAY_START)
      var lastClass: Option[Class[_]] = None
      var lastEncoder: Option[JsonWriter.WriteObject[E]] = None
      val iter = value.iterator
      while (iter.hasNext) {
        val v = iter.next()
        if (pastFirst) writer.writeByte(JsonWriter.COMMA)
        else pastFirst = true
        if (v == null || v == None) writer.writeNull()
        else {
          val currentClass = v.getClass
          if (lastClass.isEmpty || (currentClass ne lastClass.get)) {
            lastClass = Some(currentClass)
            lastEncoder = Option(json.tryFindWriter(lastClass.get).asInstanceOf[JsonWriter.WriteObject[E]])
            if (lastEncoder.isEmpty) throw new ConfigurationException(s"Unable to find writer for $lastClass")
          }
          lastEncoder.get.write(writer, v)
        }
      }
      writer.writeByte(JsonWriter.ARRAY_END)
    }
  }
}
