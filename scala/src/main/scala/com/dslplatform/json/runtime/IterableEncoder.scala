package com.dslplatform.json
package runtime

import scala.collection.Iterable

final class IterableEncoder[E](
  json: DslJson[_],
  encoder: Option[JsonWriter.WriteObject[E]]
) extends JsonWriter.WriteObject[Iterable[E]] with ExplicitDescription {

  require(manifest ne null, "manifest can't be null")
  require(encoder ne null, "encoder can't be null")

  private var lastCachedClass: Option[Class[_]] = None
  private var lastCachedWriter: Option[JsonWriter.WriteObject[E]] = None

  override def write(writer: JsonWriter, value: Iterable[E]): Unit = {
    if (value eq null) writer.writeNull()
    else if (value.isEmpty) writer.writeAscii(IterableEncoder.EMPTY)
    else if (encoder.isDefined) {
      writer.writeByte(JsonWriter.ARRAY_START)
      val enc = encoder.get
      value match {
        case iseq: scala.collection.IndexedSeq[E] =>
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
          if (lastCachedClass.contains(currentClass)) {
            lastEncoder = lastCachedWriter
            lastClass = lastCachedClass
          } else if (!lastClass.contains(currentClass)) {
            lastClass = Some(currentClass)
            lastEncoder = Option(json.tryFindWriter(lastClass.get).asInstanceOf[JsonWriter.WriteObject[E]])
            if (lastEncoder.isEmpty) {
              throw new ConfigurationException(s"Unable to find writer for $lastClass")
            }
            if (lastCachedClass.isEmpty) {
              this.synchronized {
                if (lastCachedClass.isEmpty) {
                  lastCachedWriter = lastEncoder
                  lastCachedClass = lastClass
                }
              }
            }
          }
          lastEncoder.get.write(writer, v)
        }
      }
      writer.writeByte(JsonWriter.ARRAY_END)
    }
  }
}
object IterableEncoder {
  private val EMPTY = Array[Byte]('[', ']')
}