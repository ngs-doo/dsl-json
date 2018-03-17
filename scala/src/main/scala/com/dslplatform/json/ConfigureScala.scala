package com.dslplatform.json

import com.dslplatform.json.JsonReader.ReadObject
import com.dslplatform.json.JsonWriter.WriteObject
import runtime._

class ConfigureScala extends Configuration {
  override def configure(json: DslJson[_]): Unit = {
    json.readerFactories.add(0, ScalaCollectionAnalyzer.Reader)
    json.writerFactories.add(0, ScalaCollectionAnalyzer.Writer)
    json.readerFactories.add(0, ScalaMapAnalyzer.Reader)
    json.writerFactories.add(0, ScalaMapAnalyzer.Writer)
    json.readerFactories.add(0, ScalaClassAnalyzer.Reader)
    json.writerFactories.add(0, ScalaClassAnalyzer.Writer)
    json.binderFactories.add(0, ScalaClassAnalyzer.Binder)
    json.readerFactories.add(0, ScalaTupleAnalyzer.Reader)
    json.writerFactories.add(0, ScalaTupleAnalyzer.Writer)
    json.readerFactories.add(0, OptionAnalyzer.Reader)
    json.writerFactories.add(0, OptionAnalyzer.Writer)
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
