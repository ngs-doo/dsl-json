package com.dslplatform.json
package runtime

final class ScalaMapEncoder[K, V](
  json: DslJson[_],
  checkForConversionToString: Boolean,
  keyEncoder: Option[JsonWriter.WriteObject[K]],
  valueEncoder: Option[JsonWriter.WriteObject[V]]
) extends JsonWriter.WriteObject[scala.collection.Map[K, V]] with ExplicitDescription {
  require(json ne null, "json can't be null")
  require(keyEncoder ne null, "keyEncoder can't be null")
  require(valueEncoder ne null, "valueEncoder can't be null")

  private val EMPTY = Array[Byte]('{', '}')

  override def write(writer: JsonWriter, value: scala.collection.Map[K, V]): Unit = {
    if (value eq null) writer.writeNull()
    else if (value.isEmpty) writer.writeAscii(EMPTY)
    else if (keyEncoder.isDefined && valueEncoder.isDefined) {
      val ke = keyEncoder.get
      val ve = valueEncoder.get
      writer.writeByte(JsonWriter.OBJECT_START)
      val iter = value.iterator
      val (k1, v1) = iter.next()
      if (checkForConversionToString) writeQuoted(writer, ke, k1)
      else ke.write(writer, k1)
      writer.writeByte(JsonWriter.SEMI)
      ve.write(writer, v1)
      while (iter.hasNext) {
        val (k, v) = iter.next()
        writer.writeByte(JsonWriter.COMMA)
        if (checkForConversionToString) writeQuoted(writer, ke, k)
        else ke.write(writer, k)
        writer.writeByte(JsonWriter.SEMI)
        ve.write(writer, v)
      }
      writer.writeByte(JsonWriter.OBJECT_END)
    } else {
      var pastFirst = false
      writer.writeByte(JsonWriter.OBJECT_START)
      var lastKeyClass: Option[Class[_]] = None
      var lastValueClass: Option[Class[_]] = None
      var lastKeyEncoder = keyEncoder
      var lastValueEncoder: Option[JsonWriter.WriteObject[V]] = None
      val iter = value.iterator
      while (iter.hasNext) {
        val (k, v) = iter.next()
        if (pastFirst) writer.writeByte(JsonWriter.COMMA)
        else pastFirst = true
        val currentKeyClass = k.getClass
        if (keyEncoder.isEmpty && !lastKeyClass.contains(currentKeyClass)) {
          lastKeyClass = Some(currentKeyClass)
          json.tryFindWriter(currentKeyClass) match {
            case wo: JsonWriter.WriteObject[K@unchecked] => lastKeyEncoder = Some(wo)
            case _ => throw new ConfigurationException(s"Unable to find writer for $lastKeyClass")
          }
        }
        writeQuoted(writer, lastKeyEncoder.get, k)
        writer.writeByte(JsonWriter.SEMI)
        if (valueEncoder.isDefined) valueEncoder.get.write(writer, v)
        else if (v == null || v == None) writer.writeNull()
        else {
          val currentValueClass = v.getClass
          if (!lastValueClass.contains(currentValueClass)) {
            lastValueClass = Some(currentValueClass)
            json.tryFindWriter(currentValueClass) match {
              case wo: JsonWriter.WriteObject[V@unchecked] => lastValueEncoder = Some(wo)
              case _ => throw new ConfigurationException(s"Unable to find writer for $lastValueClass")
            }
          }
          lastValueEncoder.get.write(writer, v)
        }
      }
      writer.writeByte(JsonWriter.OBJECT_END)
    }
  }

  private def writeDouble(value: Double, writer: JsonWriter): Unit = {
    if (value == Double.NaN) writer.writeAscii("NaN")
    else if (value == Double.PositiveInfinity) writer.writeAscii("Infinity")
    else if (value == Double.NegativeInfinity) writer.writeAscii("-Infinity")
    else {
      writer.writeByte(JsonWriter.QUOTE)
      NumberConverter.serialize(value, writer)
      writer.writeByte(JsonWriter.QUOTE)
    }
  }

  private def writeFloat(value: Float, writer: JsonWriter): Unit = {
    if (value == Float.NaN) writer.writeAscii("NaN")
    else if (value == Float.PositiveInfinity) writer.writeAscii("Infinity")
    else if (value == Float.NegativeInfinity) writer.writeAscii("-Infinity")
    else {
      writer.writeByte(JsonWriter.QUOTE)
      NumberConverter.serialize(value, writer)
      writer.writeByte(JsonWriter.QUOTE)
    }
  }

  private def writeQuoted(writer: JsonWriter, keyWriter: JsonWriter.WriteObject[K], key: K): Unit = {
    key match {
      case k: Double => writeDouble(k, writer)
      case k: java.lang.Double => writeDouble(k, writer)
      case k: Float => writeFloat(k, writer)
      case k: java.lang.Float => writeFloat(k, writer)
      case _: Number =>
        writer.writeByte(JsonWriter.QUOTE)
        keyWriter.write(writer, key)
        writer.writeByte(JsonWriter.QUOTE)
      case _ =>
        keyWriter.write(writer, key)
    }
  }
}
