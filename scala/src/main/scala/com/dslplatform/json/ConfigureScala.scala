package com.dslplatform.json

import com.dslplatform.json.JsonReader.ReadObject
import com.dslplatform.json.JsonWriter.WriteObject
import runtime._

class ConfigureScala extends Configuration {
  override def configure(json: DslJson[_]): Unit = {
    json.registerReaderFactory(OptionAnalyzer.Reader)
    json.registerWriterFactory(OptionAnalyzer.Writer)
    json.registerReaderFactory(ScalaMapAnalyzer.Reader)
    json.registerWriterFactory(ScalaMapAnalyzer.Writer)
    json.registerReaderFactory(ScalaTupleAnalyzer.Reader)
    json.registerWriterFactory(ScalaTupleAnalyzer.Writer)
    json.registerReaderFactory(ScalaCollectionAnalyzer.Reader)
    json.registerWriterFactory(ScalaCollectionAnalyzer.Writer)
    json.registerReaderFactory(ScalaEnumAsTraitAnalyzer.Reader)
    json.registerWriterFactory(ScalaEnumAsTraitAnalyzer.Writer)
    json.registerReaderFactory(ScalaClassAnalyzer.Reader)
    json.registerWriterFactory(ScalaClassAnalyzer.Writer)
    json.registerBinderFactory(ScalaClassAnalyzer.Binder)
    json.registerReader(classOf[BigDecimal], new ReadObject[BigDecimal] {
      override def read(reader: JsonReader[_]): BigDecimal = {
        BigDecimal(NumberConverter.deserializeDecimal(reader))
      }
    })
    json.registerWriter(classOf[BigDecimal], new WriteObject[BigDecimal] {
      override def write(writer: JsonWriter, value: BigDecimal): Unit = {
        NumberConverter.serialize(value.bigDecimal, writer)
      }
    })
    json.registerReader(classOf[BigInt], new ReadObject[BigInt] {
      override def read(reader: JsonReader[_]): BigInt = {
        BigInt(BigIntegerConverter.deserialize(reader))
      }
    })
    json.registerWriter(classOf[BigInt], new WriteObject[BigInt] {
      override def write(writer: JsonWriter, value: BigInt): Unit = {
        BigIntegerConverter.serialize(value.bigInteger, writer)
      }
    })
  }
}
