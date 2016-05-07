DSL JSON library
================

DSL Platform compatible JSON library for Java and Android.

Java JSON library designed for performance. Built for invasive software composition with DSL Platform compiler.

![JVM serializers benchmark results](https://cloud.githubusercontent.com/assets/1181401/13662269/8c49a02c-e699-11e5-9e46-f98f07fd68ef.png)

## Distinguishing features

 * supports external schema - Domain Specification Language (DSL)
 * works on existing POJO classes via annotation processor - it converts POJO to DSL schema and constructs specialized converters at compile time
 * performance - faster than any other Java JSON library. On pair with fastest binary JVM codecs
 * works on byte level - deserialization can work on byte[] or InputStream. It doesn't need intermediate char representation
 * extensibility - custom types can be registered for serialization/deserialization
 * streaming support - large JSON lists support streaming with minimal memory usage
 * zero-copy operations - converters avoid producing garbage
 * minimal size - runtime dependency weights around 100KB
 * no runtime overhead - both schema and annotation based POJOs are prepared at compile time
 * no unsafe code - library doesn't rely on Java UNSAFE/internal methods

## Schema based serialization

DSL can be used for defining schema from which POJO classes with embedded JSON conversion are constructed.
This is useful in large, multi-language projects where model is defined outside of Java classes.
More information about DSL can be found on [DSL Platform](https://dsl-platform.com) website.

## @CompiledJson annotation

Annotation processor works by translating Java classes into DSL and running DSL Platform compiler on it.
DSL compiler will generate optimized converters and register them into `META-INF/services`.
This will be loaded during `DslJson` initialization with `ServiceLoader`.
Converters will be created even for dependent objects which don't have `@CompiledJson` annotation.
This can be used to create serializers for pre-existing classes without annotating them.
Both bean properties and public non-final fields are supported.

Annotation processor can be added as Maven dependency with:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json-processor</artifactId>
      <version>0.9</version>
      <scope>provided</scope>
    </dependency>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.3</version>
      <configuration>
        <annotationProcessors>
          <annotationProcessor>com.dslplatform.json.CompiledJsonProcessor</annotationProcessor>
        </annotationProcessors>
      </configuration>
    </plugin>

	
For use in Android, Gradle can be configured with:

    apply plugin: 'android-apt'
    apt {
      processor 'com.dslplatform.json.CompiledJsonProcessor'
    }
    dependencies {
      compile compile 'com.dslplatform:dsl-json:1.0.0'
      apt 'com.dslplatform:dsl-json-processor:0.9'
    }

Project examples can be found in [examples folder](examples)

### Java/DSL property mapping

| Java type                                              | DSL type     |
| ------------------------------------------------------ | ------------ |
| int                                                    |  int         |
| long                                                   |  long        |
| float                                                  |  float       |
| double                                                 |  double      |
| boolean                                                |  bool        |
| java.lang.String                                       |  string?     |
| java.lang.Integer                                      |  int?        |
| java.lang.Long                                         |  long?       |
| java.lang.Float                                        |  float?      |
| java.lang.Double                                       |  double?     |
| java.lang.Boolean                                      |  bool?       |
| java.math.BigDecimal                                   |  decimal?    |
| java.time.LocalDate                                    |  date?       |
| java.time.OffsetDateTime                               |  timestamp?  |
| org.joda.time.LocalDate                                |  date?       |
| org.joda.time.DateTime                                 |  timestamp?  |
| byte[]                                                 |  binary      |
| java.util.UUID                                         |  uuid?       |
| java.util.Map&lt;java.lang.String,java.lang.String&gt; |  properties? |
| java.net.InetAddress                                   |  ip?         |
| java.awt.Color                                         |  color?      |
| java.awt.geom.Rectangle2D                              |  rectangle?  |
| java.awt.geom.Point2D                                  |  location?   |
| java.awt.geom.Point                                    |  point?      |
| java.awt.image.BufferedImage                           |  image?      |
| android.graphics.Rect                                  |  rectangle?  |
| android.graphics.PointF                                |  location?   |
| android.graphics.Point                                 |  point?      |
| android.graphics.Bitmap                                |  image?      |
| org.w3c.dom.Element                                    |  xml?        |

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

### Nullability annotations

During translation from Java objects into DSL schema, existing type system nullability rules are followed.
With the help of non-null annotations, hints can be introduced to work around some Java nullability type system limitations.
List of supported non-null annotations:

 * javax.validation.constraints.NotNull
 * edu.umd.cs.findbugs.annotations.NonNull
 * javax.annotation.Nonnull
 * org.jetbrains.annotations.NotNull
 * lombok.NonNull
 * android.support.annotation.NonNull

### Property aliases

Annotation processor supports external annotations for customizing property name in JSON:

 * com.fasterxml.jackson.annotation.JsonProperty
 * com.google.gson.annotations.SerializedName

Those annotations will be translated into specialized DSL for specifying serialization name.

### Ignored properties

Existing bean properties and fields can be ignored using one of the supported annotations:

 * com.fasterxml.jackson.annotation.JsonIgnore
 * org.codehaus.jackson.annotate.JsonIgnore

Ignored properties will not be translated into DSL schema.

## Serialization modes

Library has two serialization modes:

 * minimal serialization - omits default properties which can be reconstructed from schema definition
 * all properties serialization - will serialize all properties from schema definition

Best serialization performance can be obtained with combination of minimal serialization and minified property names/aliases.

## Benchmarks

Independent benchmarks can validate the performance of DSL-JSON library:

 * [JVM serializers](https://github.com/eishay/jvm-serializers/wiki) - benchmark for all kind of JVM codecs. Shows DSL-JSON as fast as top binary codecs. More up to date results available at: http://hperadin.github.io/jvm-serializers-report/report.html
 * [Techempower round 11](https://www.techempower.com/benchmarks/#section=data-r11&hw=peak&test=json) - shows 50% improvements for servlet-dsl over standard servlet utilizing Jackson
 * [Kostya JSON](https://github.com/kostya/benchmarks) - fastest performing Java JSON library

Reference benchmark (built by library authors):

 * [.NET vs JVM JSON](https://github.com/ngs-doo/json-benchmark) - comparison of various JSON libraries

## Dependencies

DSL compiler requires Mono/.NET, but only during compilation. There is no runtime Mono/.NET dependency, only JVM.
Library has optional Android and Joda-Time dependencies.
Java8 Java-Time API is supported as a separate jar, since core library targets Java6.

Library can be added as Maven dependency with:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json</artifactId>
      <version>1.0.0</version>
    </dependency>

## Best practices

Reusing reader/writer.

`JsonWriter` should be reused since it contains growable `byte[]` buffer for encoding objects into JSON.
For thread reuse use something like `ThreadLocal<JsonWriter>`.
After serialization copy resulting buffer to stream with `.toStream(OutputStream)` method.

`JsonReader` works on `byte[]` input. It's best to construct `JsonReader` with reusable `byte[]` and specifying `int` length.
For `InputStream` `JsonStreamReader` can be used. For small messages it's better to use byte based reader instead of stream based reader.

## FAQ

 ***Q***: What is `TContext` in `DslJson` and what should I use for it?  
 ***A***: Generic `TContext` is used for library specialization. Use `DslJson<Object>` when you don't need it and just provide `null` for it.
 
 ***Q***: Why is DSL JSON faster than others?  
 ***A***: Almost zero allocations. Works on byte level. Better algorithms for conversion from `byte[]` -> type and vice-versa. Minimized unexpected branching.
 
 ***Q***: DslJson is failing with unable to resolve reader/writer. What does it mean?  
 ***A***: During startup DslJson loads services through `ServiceLoader`. For this to work `META-INF/services/com.dslplatform.json.Configuration` must exist with the content of `dsl_json.json.ExternalSerialization` which is the class crated during compilation step. In certain scenarios this file is not copied to APK or to the appropriate jar/war file. You can work around it by creating such file in your project under `src/main/resources/META-INF/services` as workaround for the used package/build tool.
 
 ***Q***: Maven/Gradle are failing during compilation with @CompiledJson. What can I do about it?
 ***A***: If Mono/.NET is available it *should* work out-of-the-box. But if some strange issue occurs, detailed log can be enabled to see what is causing the issue. Log is disabled by default, since some Gradle setups fail if something is logged during compilation. Log can be enabled with `dsljson.loglevel` [processor option](examples/Maven/pom.xml#L35)
