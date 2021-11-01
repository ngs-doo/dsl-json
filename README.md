DSL-JSON library
================

Fastest JVM (Java/Android/Scala/Kotlin) JSON library with advanced compile-time databinding support. Compatible with DSL Platform.

Java JSON library designed for performance. Built for invasive software composition with DSL Platform compiler.

![JVM serializers benchmark results](https://cloud.githubusercontent.com/assets/1181401/13662269/8c49a02c-e699-11e5-9e46-f98f07fd68ef.png)

## Distinguishing features

 * supports external schema - Domain Specification Language (DSL)
 * works on existing POJO classes via annotation processor
 * performance - faster than any other Java JSON library. On par with fastest binary JVM codecs
 * works on byte level - deserialization can work on byte[] or InputStream. It doesn't need intermediate char representation
 * extensibility - support for custom types, custom analyzers, annotation processor extensions...
 * streaming support - large JSON lists support streaming with minimal memory usage
 * zero-copy operations - converters avoid producing garbage
 * minimal size - runtime dependency weights around 200KB
 * no unsafe code - library doesn't rely on Java UNSAFE/internal methods
 * POJO <-> object and/or array format - array format avoids serializing names, while object format can be used in minimal serialization mode
 * legacy name mapping - multiple versions of JSON property names can be mapped into a single POJO using alternativeNames annotation
 * binding to an existing instance - during deserialization an existing instance can be provided to reduce GC
 * generics, builder pattern, factory pattern and ctor with arguments - Java8 version supports all relevant initialization methods
 * compile time detection of unsafe conversion - Java8 version can throw compile time error for conversion which can fail at runtime
 * advanced annotation processor support - support for Java-only compilation or DSL Platform integration via conversion of Java code to DSL schema
 * customizable runtime overheads - works in reflection mode, in Java8 annotation processor mode or DSL Platform mode. Schema and annotation based POJOs are prepared at compile time
 * support for other library annotations - Jackson and JsonB annotations will be used and compile time analysis can be extended in various ways
 * Scala types support - Scala collections, primitives and boxed primitives work without any extra annotations or configuration
 * Kotlin support - annotation processor can be used from Kotlin. NonNull annotation is supported
 * JsonB support - high level support for JsonB String and Stream API. Only minimal support for configuration

## Schema based serialization

DSL can be used for defining schema from which POJO classes with embedded JSON conversion are constructed.
This is useful in large, multi-language projects where model is defined outside of Java classes.
More information about DSL can be found on [DSL Platform](https://dsl-platform.com) website.

## @CompiledJson annotation

Annotation processor works by analyzing Java classes and its explicit or implicit references.
Processor outputs encoding/decoding code/descriptions at compile time.
This avoids the need for reflection, provides compile time safety and allows for some advanced configurations.
Processor can register optimized converters into `META-INF/services`.
This will be loaded during `DslJson` initialization with `ServiceLoader`.
Since v1.8.0 naming conventions will be used for Java8 converters (`package._NAME_DslJsonConverter`) which works even without loading services upfront.
Converters will be created even for dependent objects which don't have `@CompiledJson` annotation.
This can be used to create serializers for pre-existing classes without annotating them.

### Java8 annotation processor

Since v1.7.0 DSL-JSON supports compile time databinding without Mono/.NET dependency.
It provides most features and flexibility, due to integration with runtime analysis and combining of various generic analysis.
Bean properties, public fields, classes without empty constructor, factories and builder patterns are supported.
Package private classes and factory methods can be used.
Array format can be used for efficient payload transfer.

To use Java8 annotation processor its sufficient to just reference Java8 version of the library:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json-java8</artifactId>
      <version>1.9.9</version>
    </dependency>

For use in Android, Gradle can be configured with:

    android {
      compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
      }
    }
    dependencies {
      compile 'com.dslplatform:dsl-json-java8:1.9.9'
      annotationProcessor 'com.dslplatform:dsl-json-java8:1.9.9'
      provided 'javax.json.bind:javax.json.bind-api:1.0'
    }

To use DSL-JSON on older Android versions setup should be done without initializing Java8 specific types:

    private final DslJson<Object> dslJson = new DslJson<Object>(runtime.Settings.basicSetup());


### DSL Platform annotation processor

DSL Platform annotation processor requires .NET/Mono to create databindings.
It works by translating Java code into equivalent DSL schema and running DSL Platform compiler on it.
Since v1.7.2 Java8 version has similar performance, so the main benefit is ability to target Java6.
Bean properties, public non-final fields and only classes with empty constructor are supported.
Only object format is supported.

**If you are not sure which annotation processor to use, you should probably use the Java8 version instead of the DSL Platform one**.
DSL Platform annotation processor can be added as Maven dependency with:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json-processor</artifactId>
      <version>1.9.9</version>
      <scope>provided</scope>
    </dependency>

For use in Android, Gradle can be configured with:

    dependencies {
      compile 'com.dslplatform:dsl-json:1.9.9'
      annotationProcessor 'com.dslplatform:dsl-json-processor:1.9.9'
    }

Project examples can be found in [examples folder](examples)

### Java/DSL property mapping

| Java type                 | DSL type     | Java type                          | DSL type     |
| ------------------------- | ------------ | ---------------------------------- | ------------ |
| int                       |  int         | byte[]                             |  binary      |
| long                      |  long        | java.util.Map&lt;String,String&gt; |  properties? |
| float                     |  float       | java.net.InetAddress               |  ip?         |
| double                    |  double      | java.awt.Color                     |  color?      |
| boolean                   |  bool        | java.awt.geom.Rectangle2D          |  rectangle?  |
| java.lang.String          |  string?     | java.awt.geom.Point2D              |  location?   |
| java.lang.Integer         |  int?        | java.awt.geom.Point                |  point?      |
| java.lang.Long            |  long?       | java.awt.image.BufferedImage       |  image?      |
| java.lang.Float           |  float?      | android.graphics.Rect              |  rectangle?  |
| java.lang.Double          |  double?     | android.graphics.PointF            |  location?   |
| java.lang.Boolean         |  bool?       | android.graphics.Point             |  point?      |
| java.math.BigDecimal      |  decimal?    | android.graphics.Bitmap            |  image?      |
| java.time.LocalDate       |  date?       | org.w3c.dom.Element                |  xml?        |
| java.time.OffsetDateTime  |  timestamp?  | org.joda.time.LocalDate            |  date?       |
| java.util.UUID            |  uuid?       | org.joda.time.DateTime             |  timestamp?  |


### Java/DSL collection mapping

| Java type             | DSL type      |
| --------------------- | ------------- |
| array                 |  Array        |
| java.util.List        |  List         |
| java.util.Set         |  Set          |
| java.util.LinkedList  |  Linked List  |
| java.util.Queue       |  Queue        |
| java.util.Stack       |  Stack        |
| java.util.Vector      |  Vector       |
| java.util.Collection  |  Bag          |

Collections can be used on supported Java types, other POJOs and enums.

### Other collections/containers

Java8 supports all kinds of collections, even maps and Java8 specific container such as Optional.

### Custom types in compile-time databinding

Types without builtin mapping can be supported in three ways:

 * by implementing `JsonObject` and appropriate `JSON_READER`
 * by defining custom conversion class and annotating it with `@JsonConverter`
 * by defining custom conversion class and referencing it from property with converter through `@JsonAttribute`

Custom converter for `java.util.Date` can be found in [example project](examples/MavenJava6/src/main/java/com/dslplatform/maven/Example.java#L116) 
Annotation processor will check if custom type implementations have appropriate signatures.
Converter for `java.util.ArrayList` can be found in [same example project](examples/MavenJava6/src/main/java/com/dslplatform/maven/Example.java#L38) 

`@JsonConverter` which implements `Configuration` will also be registered in `META-INF/services` which makes it convenient to [setup initialization](examples/MavenJava6/src/main/java/com/dslplatform/maven/ImmutablePerson.java#L48).

All of the above custom type examples work out-of-the-box in Java8 version of the library.


### @JsonAttribute features

DSL-JSON property annotation supports several customizations/features:

 * name - define custom serialization name
 * alternativeNames - different incoming JSON attributes can be mapped into appropriate property. This can be used for simple features such as casing or for complex features such as model evolution
 * ignore - don't serialize specific property into JSON
 * nullable - tell compiler that this property can't be null. Compiler can remove some checks in that case for minuscule performance boost
 * mandatory - mandatory properties must exists in JSON. Even in omit-defaults mode. If property is not found, `IOException` will be thrown
 * index - defines index order used during serialization or can be used for array format
 * hashMatch - DSL-JSON matches properties by hash values. If this option is turned off exact comparison will be performed which will add minor deserialization overhead, but invalid properties with same hash names will not be deserialized into "wrong" property. In case when model contains multiple properties with same hash values, compiler will inject exact comparison by default, regardless of this option value.
 * converter - custom conversion per property. Can be used for formatting or any other custom handling of JSON processing for specific property
 * typeSignature - disable inclusion of $type during abstract type serialization. By default abstract type will include additional information which is required for correct deserialization. Abstract types can be deserialized into a concreted type by defining `deserializeAs` on `@CompiledJson` which allows the removal of $type during both serialization and deserialization

### @JsonValue enum feature

Java8 version supports converting enum as custom value.
To use such feature @JsonValue annotation must be placed on method or field.

### JSON pretty print

Formatted output with alignments and newlines can be created via `PrettifyOutputStream`.

    dslJson.serialize(instance, new PrettifyOutputStream(outputStream));

### External annotations

For existing classes which can't be modified with `@JsonAttribute` alternative external annotations are supported:

#### Nullability annotations

During translation from Java objects into DSL schema, existing type system nullability rules are followed.
With the help of non-null annotations, hints can be introduced to work around some Java nullability type system limitations.
List of supported non-null annotations can be found in [processor source code](processor/src/main/java/com/dslplatform/json/CompiledJsonProcessor.java#L88)

#### Property aliases

Annotation processor supports external annotations for customizing property name in JSON:

 * com.fasterxml.jackson.annotation.JsonProperty
 * com.google.gson.annotations.SerializedName

Those annotations will be translated into specialized DSL for specifying serialization name.

#### Ignored properties

Existing bean properties and fields can be ignored using one of the supported annotations:

 * com.fasterxml.jackson.annotation.JsonIgnore
 * org.codehaus.jackson.annotate.JsonIgnore

Ignored properties will not be translated into DSL schema.

#### Required properties

Jackson `required = true` can be used to fail if property is missing in JSON:

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

## Dependencies

Core library (with analysis processor) and DSL Platform annotation processor targets Java6.
Java8 library includes runtime analysis, reflection support, annotation processor and Java8 specific types. When Java8 annotation processor is used Mono/.NET doesn't need to be present on the system.
Android can use Java8 version of the library even on older versions due to lazy loading of types which avoids loading types Android does not support. 

If not sure which version to use, use Java8 version of the library with annotation processor.

## Runtime analysis

Java8 library has builtin runtime analysis support, so library can be used even without compile time databinding 
or it can just add additional runtime support alongside compile-time databinding (default behavior). 
Runtime analysis is required for some features such as generics which are not known at compile time.
Runtime analysis works by lazy type resolution from registered converters, eg:

    private final DslJson.Settings settings = runtime.Settings.withRuntime().includeServiceLoader();
    private final DslJson<Object> json = new DslJson<Object>(settings);

## Best practices

### Reusing reader/writer.

`JsonWriter` It has two modes of operations:
 
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
Also, since v1.5 binding API is available which can reuse instances for deserialization.

    DslJson<Object> json = new DslJson<Object>(); //always reuse
    byte[] bytes = "{\"number\":123}".getBytes("UTF-8");
    JsonReader<Object> reader = json.newReader().process(bytes, bytes.length);
    POJO instance = new POJO(); //can be reused
    POJO bound = reader.next(POJO.class, instance); //bound is the same as instance above

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

    libraryDependencies += "com.dslplatform" %% "dsl-json-scala" % "1.9.9"

### Kotlin support

Kotlin has excellent Java interoperability, so annotation processor can be used as-is.
When used with Gradle, configuration can be done via:

    apply plugin: 'kotlin-kapt'
    dependencies {
      compile "com.dslplatform:dsl-json-java8:1.9.9"
      kapt "com.dslplatform:dsl-json-java8:1.9.9"
    }

## FAQ

 ***Q***: What is `TContext` in `DslJson` and what should I use for it?  
 ***A***: Generic `TContext` is used for library specialization. Use `DslJson<Object>` when you don't need it and just provide `null` for it.
 
 ***Q***: Why is DSL-JSON faster than others?  
 ***A***: Almost zero allocations. Works on byte level. Better algorithms for conversion from `byte[]` -> type and vice-versa. Minimized unexpected branching. Reflection version is comparable with Jackson performance. Extra difference comes from compile-time databinding.
 
 ***Q***: DslJson is failing with unable to resolve reader/writer. What does it mean?  
 ***A***: During startup DslJson loads services through `ServiceLoader`. For this to work `META-INF/services/com.dslplatform.json.Configuration` must exist with the content of `dsl_json_Annotation_Processor_External_Serialization` or `dsl_json.json.ExternalSerialization` which is the class crated during compilation step. Make sure you've referenced processor library (which is responsible for setting up readers/writers during compilation) and double check if annotation processor is running. Refer to [example projects](examples) for how to set up environment. As of v1.8.0 Java8 version of the library avoids this issue since services are not used by default anymore in favor of named based convention. Eclipse is known to create problems with annotation processor since it requires manual setup (instead of using pom.xml setup). For Eclipse the best workaround is to build with Maven instead or relying on its build tools.
 
 ***Q***: Maven/Gradle are failing during compilation with `@CompiledJson` when I'm using DSL Platform annotation processor. What can I do about it?  
 ***A***: If Mono/.NET is available it *should* work out-of-the-box. But if some strange issue occurs, detailed log can be enabled to see what is causing the issue. Log is disabled by default, since some Gradle setups fail if something is logged during compilation. Log can be enabled with `dsljson.loglevel` [processor option](examples/MavenJava6/pom.xml#L35)

 ***Q***: DSL Platform annotation processor checks for new DSL compiler version on every compilation. How can I disable that?  
 ***A***: If you specify custom `dsljson.compiler` processor option or put `dsl-compiler.exe` in project root it will use that one and will not check online for updates

 ***Q***: I get compilation error when annotation procesor runs. What can I do?  
 ***A***: Common error is missing dependency on Java 9+ for annotation marker. You can add such dependency on configure compiler arguments to exclude it via `dsljson.generatedmarker`. Otherwise its best to inspect the generated code, look if there is some configuration error, like referencing class without sufficient visibility. If there is nothing wrong with the setup, there might be a bug with the DSL-JSON annotation processor in which case it would be helpful to provide a minimal reproducible

 ***Q***: What is this DSL Platform?  
 ***A***: DSL Platform is a proprietary compiler written in C#. Since v1.7.0 DSL Platform is no longer required to create compile-time databinding. Compiler is free to use, but access to source code is licensed. If you want access to the compiler or need performance consulting [let us know](https://dsl-platform.com)
