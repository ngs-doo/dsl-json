DSL-JSON library
================

Fastest JVM (Java/Android/Scala/Kotlin) JSON library with advanced compile-time databinding support.

Java JSON library designed for performance. Originally built for invasive software composition with DSL Platform compiler.

![JVM serializers benchmark results](https://cloud.githubusercontent.com/assets/1181401/13662269/8c49a02c-e699-11e5-9e46-f98f07fd68ef.png)

## Distinguishing features

 * works on existing POJO classes via annotation processor
 * performance - faster than any other Java JSON library. On par with fastest binary JVM codecs
 * works on byte level - deserialization can work on byte[] or InputStream. It doesn't need intermediate char representation
 * extensibility - support for custom types, custom analyzers, annotation processor extensions...
 * streaming support - large JSON lists support streaming with minimal memory usage
 * allocation friendly - converters avoid producing garbage
 * minimal size - runtime dependency weights around 450KB
 * no unsafe code - library doesn't rely on Java UNSAFE/internal methods
 * POJO <-> object and/or array format - array format avoids serializing names, while object format can be used in minimal serialization mode
 * legacy name mapping - multiple versions of JSON property names can be mapped into a single POJO using alternativeNames annotation
 * binding to an existing instance - during deserialization an existing instance can be provided to reduce GC
 * generics, builder pattern, factory pattern and ctor with arguments - all relevant initialization methods are supported
 * compile time detection of unsafe conversion - can throw compile time error for conversion which can fail at runtime
 * customizable runtime overheads - works in reflection mode or annotation processor mode. Annotation based POJOs are prepared at compile time
 * support for other library annotations - Jackson and JsonB annotations will be used and compile time analysis can be extended in various ways
 * Scala types support - Scala collections, primitives and boxed primitives work without any extra annotations or configuration
 * Kotlin support - annotation processor can be used from Kotlin. NonNull annotation is supported
 * JsonB support - high level support for JsonB String and Stream API. Only minimal support for configuration
 * compatible with [DSL Platform](DSL.md)

## @CompiledJson annotation

Annotation processor works by analyzing Java classes and its explicit or implicit references.
Processor outputs encoding/decoding code/descriptions at compile time.
This avoids the need for reflection, provides compile time safety and allows for some advanced configurations.

By default, library only searches for `@CompiledJson` annotation, but it can be configured to search for `@JacksonCreator` and `@JsonbCreator`.  
Converters will be created even for dependent objects which don't have relevant annotation.
This can be used to create serializers for pre-existing classes without annotating them.

There are 2 main ways how generated code/manual services are detected:
  * with lookups from `META-INF/services` through `ServiceLoader` during `DslJson` initialization
  * by probing for name conventions: `package._NAME_DslJsonConverter` when required

### Annotation processor

Annotation processor provides most features and flexibility, due to integration with runtime analysis and combining of various generic analysis.
Bean properties, public fields, classes without empty constructor, factories and builder patterns are supported.
Package private classes and factory methods can be used.
Array format can be used for efficient payload transfer.

To use annotation processor it is sufficient to just reference the library:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json</artifactId>
      <version>2.0.0</version>
    </dependency>

For use in Android, Gradle can be configured with:

    android {
      compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
      }
    }
    dependencies {
      compile 'com.dslplatform:dsl-json:2.0.0'
      annotationProcessor 'com.dslplatform:dsl-json:2.0.0'
      provided 'javax.json.bind:javax.json.bind-api:1.0'
    }

Project examples can be found in [examples folder](examples)

### Custom types in compile-time databinding

Types without built-in mapping can be supported in three ways:

 * by defining custom conversion class and annotating it with `@JsonConverter`
 * by defining custom conversion class and referencing it from property with converter through `@JsonAttribute`
 * by implementing `JsonObject` and appropriate `JSON_READER`

Custom converter for `java.time.LocalTime` can be found in [example project](examples/MavenJava/src/main/java/com/dslplatform/maven/Example.java#L182) 
Annotation processor will check if custom type implementations have appropriate signatures.

`@JsonConverter` which implements `Configuration` will also be registered in `META-INF/services` which makes it convenient to [setup initialization](examples/MavenJava6/src/main/java/com/dslplatform/maven/ImmutablePerson.java#L48).

All of the above custom type examples work out-of-the-box.

Custom converter is a class with 2 static methods, eg:

	public static abstract class LocalTimeConverter {
		public static LocalTime read(JsonReader reader) throws IOException {
			return LocalTime.parse(reader.readSimpleString());
		}
		public static void write(JsonWriter writer, LocalTime value) {
			writer.writeString(value.toString());
		}
	}

For mutable objects 3rd optional method is supported (bind)

### @JsonAttribute features

DSL-JSON property annotation supports several customizations/features:

 * name - define custom serialization name
 * alternativeNames - different incoming JSON attributes can be mapped into appropriate property. This can be used for simple features such as casing or for complex features such as model evolution
 * ignore - don't serialize specific property into JSON
 * nullable - tell compiler that this property can't be null. Compiler can remove some checks in that case for minuscule performance boost
 * mandatory - mandatory properties must exist in JSON. Even in omit-defaults mode. If property is not found, `IOException` will be thrown
 * index - defines index order used during serialization or can be used for array format
 * hashMatch - DSL-JSON matches properties by hash values. If this option is turned off exact comparison will be performed which will add minor deserialization overhead, but invalid properties with same hash names will not be deserialized into "wrong" property. In case when model contains multiple properties with same hash values, compiler will inject exact comparison by default, regardless of this option value.
 * converter - custom conversion per property. Can be used for formatting or any other custom handling of JSON processing for specific property
 * typeSignature - disable inclusion of $type during abstract type serialization. By default, abstract type will include additional information which is required for correct deserialization. Abstract types can be deserialized into a concreted type by defining `deserializeAs` on `@CompiledJson` which allows the removal of $type during both serialization and deserialization

### @JsonValue enum feature

Library supports converting enum as custom value.
To use such feature @JsonValue annotation must be placed on method or field.

### JSON pretty print

Formatted output with alignments and newlines can be created via `PrettifyOutputStream`.

    dslJson.serialize(instance, new PrettifyOutputStream(outputStream));

### External annotations

For existing classes which can't be modified with `@JsonAttribute` alternative external annotations are supported:

#### Nullability annotations

During translation from Java objects into DSL schema, existing type system nullability rules are followed.
With the help of non-null annotations, hints can be introduced to work around some Java nullability type system limitations.
List of supported non-null annotations can be found in [processor source code](java/src/main/java/com/dslplatform/json/processor/CompiledJsonAnnotationProcessor.java#L68)

Nullable annotations can be disabled via configuration parameter `dsljson.nullable=false`. This might be useful if you want to handle null checks after deserialization.

#### Property aliases

Annotation processor supports external annotations for customizing property name in JSON:

 * com.fasterxml.jackson.annotation.JsonProperty
 * com.google.gson.annotations.SerializedName

Those annotations will be translated into specialized DSL for specifying serialization name.

#### Ignored properties

Existing bean properties and fields can be ignored using one of the supported annotations:

 * com.fasterxml.jackson.annotation.JsonIgnore
 * org.codehaus.jackson.annotate.JsonIgnore

#### Required/mandatory properties

Jackson `required = true` can be used to fail if property is missing in JSON.
DSL-JSON has distinct behavior for required (non-null) and mandatory specification.

Mandatory means that property must be present in JSON. If it's not present an error will be thrown during deserialization.
Non-null means that property cannot be null. But most types have default values and if omitted will assume this default value, eg:

  * String - empty string
  * Number - 0
  * Boolean - false
  * etc...

Some types don't have default value and for them, non-null also implies mandatory.
This behavior can be controlled via several options.

## Serialization modes

Library has several serialization modes:

 * minimal serialization - omits default properties which can be reconstructed from schema definition
 * all properties serialization - will serialize all properties from schema definition
 * array format - object will be serialized as array without property names

Best serialization performance can be obtained with combination of minimal serialization and minified property names/aliases or array format.
Object and array formats can be combined. POJO can support both formats at once.

## Benchmarks

Independent benchmarks can validate the performance of DSL-JSON library:

 * [JVM serializers](https://github.com/eishay/jvm-serializers/wiki) - benchmark for all kind of JVM codecs. Shows DSL-JSON as fast as top binary codecs
 * [Kostya JSON](https://github.com/kostya/benchmarks) - fastest performing Java JSON library
 * [JMH JSON benchmark](https://github.com/fabienrenaud/java-json-benchmark) - benchmarks for Java JSON libraries

Reference benchmark (built by library authors):

 * [.NET vs JVM JSON](https://github.com/ngs-doo/json-benchmark) - comparison of various JSON libraries

## Runtime analysis

Library has built-in runtime analysis support, so library can be used even without compile time databinding
or it can just add additional runtime support alongside compile-time databinding (default behavior). 
Runtime analysis is required for some features such as generics which are not known at compile time.
Runtime analysis works by lazy type resolution from registered converters, eg:

    private final DslJson.Settings settings = runtime.Settings.withRuntime().includeServiceLoader();
    private final DslJson<Object> json = new DslJson<Object>(settings);

## Best practices

### Reusing reader/writer.

`JsonWriter` has two modes of operations:
 
 * populating the entire output into `byte[]`
 * targeting output stream and flushing local `byte[]` to target output stream
 
`JsonWriter` can be reused via `reset` methods which binds it to specified target.
When used directly it should be always created via `newWriter` method on `DslJson` instance. 
Several `DslJson` serialize methods will reuse the writer via thread local variable.
When using `JsonWriter` via the first mode, result can be copied to stream via `.toStream(OutputStream)` method.

    DslJson<Object> json = ... // always reuse
    OutputStream stream = ... // stream with JSON in UTF-8
    json.serialize(pojo, stream); //will use thread local writer
    
`JsonReader` can process `byte[]` or `InputStream` inputs. It can be reused via the `process` methods. 
When calling `DslJson` deserialize methods often exists in two flavors:

 * with `byte[]` argument, in which case a new `JsonReader` will be created, but for best performance `byte[]` should be reused
 * without `byte[]` argument in which case thread local reader will be reused 

For small messages it's better to use `byte[]` API. When reader is used directly it should be always created via `newReader` method on `DslJson` instance.

    DslJson<Object> json = ... // always reuse
    InputStream stream = ... // stream with JSON in UTF-8
    POJO instance = json.deserialize(POJO.class, stream); //will use thread local reader

### Binding

`JsonReader` has `iterateOver` method for exposing input collection as consumable iterator.

    DslJson<Object> json = new DslJson<Object>(); //always reuse
    byte[] bytes = "{\"number\":123}".getBytes("UTF-8");
    JsonReader<Object> reader = json.newReader().process(bytes, bytes.length);
    POJO instance = new POJO(); //can be reused
    POJO bound = reader.next(POJO.class, instance); //bound is the same as instance above

Binding API is available which can reuse instances for deserialization. This is supported in converters
via 3rd optional `JSON_BINDER` member or `bind` method, eg:

    class POJO {
      public String mutableValue;
    }
    class PojoConverter {
      public POJO bind(JsonReader reader, POJO instance) {
        if (instance == null) instance = new POJO();
        instance.mutableValue = StringConverter.read(reader);
      }
    }

### Limits

Library has various configurable limits built-in to protect against malicious input:

 * default of 512 digits
 * default of 128MB strings

### String API

DSL-JSON works on byte level. To discourage use of String for JSON processing there is no high level String API.
If one really needs to use String, manual conversion is required.

    DslJson<Object> json = new DslJson<Object>();
    byte[] bytes = "{\"number\":123}".getBytes("UTF-8"); //convert string to UTF-8 bytes
    POJO instance = json.deserialize(POJO.class, bytes, bytes.length);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    json.serialize(instance, os);
    String outputAsString = os.toString("UTF-8"); //convert stream to string

### Scala support

Scala types can be used. They will be analyzed at runtime with. Scala specific behaviour:

 * Option[_] - means that JSON attribute can be null. If type is not an Option and null is found, IOException will be thrown
 * Container[Primitive] - eg: `Option[Int]` - will behave as `Option<int>` and thus it will avoid wrong type decoding issues
 * name: Type = Default - will be used to imply if attribute can be omitted from JSON - in which case the specified default value will be used (default values are static at analysis time)
 * tuples - will be encoded/decoded in Array format (without property names)
 
To avoid some Java/Scala conversion issues it's best to use Scala specific API via

    import com.dslplatform.json._ // import pimping
    val dslJson = new DslJson[Any]()
    //use encode pimp to correctly analyze types (this will mostly provide some performance benefits)
    dslJson.encode(instance, ...)
    //use decode pimp to correctly analyze types (this will avoid some issues with nested classes and missing metadata)
    val result = dslJson.decode[TargetType](...) 

For SBT dependency can be added as:

    libraryDependencies += "com.dslplatform" %% "dsl-json-scala" % "2.0.0"

### Kotlin support

Kotlin has excellent Java interoperability, so annotation processor can be used as-is.
When used with Gradle, configuration can be done via:

    plugins {
      kotlin("kapt") version "1.8.0"
    }
    dependencies {
      implementation("com.dslplatform:dsl-json:2.0.0")
      kapt("com.dslplatform:dsl-json:2.0.0")
    }

## FAQ

 ***Q***: What is `TContext` in `DslJson` and what should I use for it?  
 ***A***: Generic `TContext` is used for library specialization. Use `DslJson<Object>` when you don't need it and just provide `null` for it.

 ***Q***: How do I use DSL-JSON with String?  
 ***A***: Don't do that. You should prefer Streams or byte[] instead. If you really, really need it, it is mentioned in this README.
 
 ***Q***: Why is DSL-JSON faster than others?  
 ***A***: Almost zero allocations. Works on byte level. Better algorithms for conversion from `byte[]` -> type and vice-versa. Minimized unexpected branching. Reflection version is "comparable" with Jackson performance. Extra difference comes from compile-time databinding.

 ***Q***: I get compilation error when annotation processor runs. What can I do?  
 ***A***: Common error is missing dependency on Java 9+ for annotation marker. You can add such dependency on configure compiler arguments to exclude it via `dsljson.generatedmarker`. Otherwise, it's best to inspect the generated code, look if there is some configuration error, like referencing class without sufficient visibility. If there is nothing wrong with the setup, there might be a bug with the DSL-JSON annotation processor in which case it would be helpful to provide a minimal reproducible

 ***Q***: Can you please help me out with...?  
 ***A***: There is only so many hours in a day. You can support the library by asking for [support contract](mailto:rikard@ngs.hr?subject=DSL-JSON) with your company.
