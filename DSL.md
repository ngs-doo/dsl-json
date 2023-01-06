DSL Platform JSON library
=========================

DSL-JSON was born as an attempt to show other developers that DSL Platform is not jet another ORM.
When talking about abstract, unfamiliar stuff, some developers can argue all kind of viewpoints,
especially when they don't need to back them up with numbers.

So, during 2014, as an attempt simplify/resolve such discussions and have concrete numbers,
a new target was added to DSL Platform - generating optimized JSON conversion.

Its rather unfortunate that to get much better number one had to reinvent all kind of wheels,
in this case JSON library which tried to reduce allocation and branching with several
optimizations on how to improve specific type conversions. 
As final touch, replacing reflection with compiled code was supposed to simplify reasoning
(you can just look at generated code and debug it) and performance (too much reflection is not really healthy). 

## Used optimizations

Various optimizations were used in the initial library to get significant improvement over best libraries at the time.
But it was nothing crazy, just regular simple code with occasional bit shifting and maybe few exotic operations. 

### Work with bytes instead of chars

Many people consider JSON as string even today. Its human-readable, so obviously it is a string.
But strings are just a sequence of bytes and in case of JSON a sequence of UTF-8 bytes.
So instead of treating it as a string or an array of chars, just treat it as array of bytes
and apply special UTF-8 rules when required (which is not all that often).

### Converters without "unnecessary" allocations

By not using strings, but rather bytes one starts to see allocations all around him.
So major improvement comes from specialized converters which work on bytes directly and
will consider all kind of inputs as sequence of bytes which can be processed using just primitive Java types.
For example, JSON number is not a string, but rather somewhat bloated representation of a number with many bytes.
Eg number `123` is represented with 3 bytes in JSON, while it would take 4 bytes in most binary encodings.
Of course, larger numbers, eg: `1234567` will take 7 bytes in JSON, but still only 4 bytes in most binary encodings.
But then... if one wants to parse a date, eg `"2022-02-24"` as long as you know it is a date you can parse it as 3 numbers,
consisting from a year, month and a day.

### Schema encoded in Schema-less JSON

If one considers JSON just a transport protocol between two endpoints which are aware of same schema on both sides,
various optimizations become possible. This way `"<string>"` does not need to be parsed as string,
especially if different type is expected, such as date or enum. While its kind of futile trying to
optimize JSON if you don't have knowledge about expected schema, there will be vast number of optimizations
to perform if you don't need to parse JSON into generic tokens, only to be replaced into final types.
Just parse JSON into final types and skip over intermediary representation.

### Reducing JSON size due to knowledge of actual Schema

Go-to .NET JSON library had a nice optimization of omitting values from JSON which it could infer from object schema.
Eg, there is no need to encode `0` or `false` for `integer` or `boolean` property which is non-nullable.
This wasn't really a standard practice in Java world, so it became significant when useful (even today 
this optimization reduces one of my favorite endpoint from 64kB to <1kB for most cases as endpoint
consists from thousands of boolean fields specifying if a field is used or not).

The most extreme example of this if when you encode Java objects in JSON array notation,
which is valid JSON and very useful as response for table-like responses.
Consider object with 3 properties, eg `int i, bool b and string s`, when returned as a collection
one could send JSON as

    [
      {"i":100,"b":true,"s":"abc"},
      ...
      {"i":500,"b":false,"s":"def"}
    ]

or in a more compact format, assuming that you are aware of the schema on both sides:

    [
      [100,true,"abc"],
      ...
      [500,false,"def"]
    ]

This is rather natural thing to do in many endpoints, but really underused in the wild.

### Optimize for the common case

While solving the problem for all the cases is the baseline one should expect,
as applications should first be correct and only then performant if one has a common usage in mind
specific optimizations can be employed with that case in mind.
An example of this would be BigDecimal type which one could argue requires complex parsing
to account for all the edge cases: too big of a number, too many decimals, etc...
But if one has a notion of BigDecimal mostly as a fixed decimal number mostly used for Money,
it becomes clear that most of the time long is sufficient for parsing that kind of JSON input,
as long can hold up to 20 digits, which should be cover most cases for Money.
Even if people want to quote Money, so Javascript does not mess up the actual amount,
by knowing the schema this can still be parsed in an optimized way most of the time.
In case there is more than 20 digits, or exponent is used, it is rather cheap to switch over to general case
of parsing BigDecimal input.

### Bit-aware String parsing

If one works with bits a lot, he can find out all kind of cases which could be optimized compared to "common code".
Marko Elezovic contributed one such bit twiddling optimization to common case string parsing which reads as

    if ((bb ^ '\\') < 1) break;

Its sole purpose is to break out of the common case ascii parsing into a more general case of UTF-8 parsing,
but when most of your strings are ASCII, this has a significant impact.

### Numbers as bytes

In C and other unmanaged languages, it is common to employ various optimizations to squeeze more performance out of the code.
One common optimization is encoding several "numbers" as bytes into a single number.
Assuming that we know we are only dealing with a single digit number (0-9) and knowing that we want to store several numbers
at a single location, to store number such as 253 we can store this as a sequence of 4 bytes consisting from `0 2 5 3`.
Then when one wants to write a 4 digit number in JSON it can use this lookup table and:
  * divide by 1000 to get the first number
  * lookup from a table the other 3 numbers by shifting them at appropriate locations

In practice this looks like:

	int q = value / 1000;
	int v = DIGITS[value - q * 1000];
	buf[pos] = (byte) (q + '0');
	buf[pos + 1] = (byte) (v >> 16);
	buf[pos + 2] = (byte) (v >> 8);
	buf[pos + 3] = (byte) v;

where you even avoid to do costly modulo in favor of multiplication and subtraction.

### Keys are just identifiers

When you know the schema, you should know all keys upfront, which means that when writing JSON you can just
copy them from pre-defined byte array into output.
But nice optimization can be employed during reading as you don't need to "allocate string" to compare identifiers.
One can just keep identifier in the parsed byte array buffer and compare that. Going further this identifier can be parsed as a pure number,
representing the hash of the relevant identifier.

Eg, when parsing `{"number":123}`, during start of parsing `"number"` part of the JSON,
since we know this is an identifier, we can pin the location of byte array and start calculating hash of a value inside the string.
If hash values match to the expected one (which we knew before parsing) we can just compare the byte array from the pinned location 
(if we even want to do that - sometimes it is just good enough that we have found the same hash).

### Reflection can be significant

While there are various other optimizations in the library, one major optimizations is instead of having reflection heavy processing,
when schema is known one can write code optimized for the known schema.
This does contribute significantly to the final numbers, although most of the speedup
comes from the optimized converters and removal of unnecessary allocation.
Still, it was common practice to compile code for performance reasons instead of interpret it;
and this is kind of similar in a sense by having code look like

    instance.field1 = ConverterType1.read(input);
    instance.field2 = ConverterType2.read(input);
    instance.field3 = ConverterType3.read(input);

vs more generic solution of

    for (FieldType ft : instanceFields) {
      instance.setValue(ft.fieldName, ft.converter.read(input);
    }

And even though more generic solution might be less code, its more unpredictable and thus less optimizable.

## Current state of DSL Platform integration

Currently, DSL-JSON targets Java6 for base library (as it was developed in 2014) but due to
unfortunate history and the state of industry, it is not really all that useful to have it as an annotation processor,
especially once DSL-JSON upgrades the baseline to Java8.

So some time in the future, `dsl-json` and `dsl-json-java8` project will be merged, while `dsl-json-processor` will be deleted.
DSL Platform will still be able to use DSL-JSON, but there will be no point in generating DSL to create optimized converters, 
as Java8 project will have superset of that behavior.

Until then, if one wants to use DSL Platform with DSL-JSON through DSL-JSON (which is not really a natural way to use DSL Platform)
the rest of README provides more information.

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
Converters will be created even for dependent objects which don't have `@CompiledJson` annotation.
This can be used to create serializers for pre-existing classes without annotating them.

### DSL Platform annotation processor

DSL Platform annotation processor requires .NET/Mono to create databindings.
It works by translating Java code into equivalent DSL schema and running DSL Platform compiler on it.
Since v1.7.2 Java8 version has similar performance, so the main benefit is ability to target Java6.
Bean properties, public non-final fields and only classes with empty constructor are supported.
Only object format is supported.

DSL Platform annotation processor can be added as Maven dependency with:

    <dependency>
      <groupId>com.dslplatform</groupId>
      <artifactId>dsl-json-processor</artifactId>
      <version>1.10.0</version>
      <scope>provided</scope>
    </dependency>

For use in Android, Gradle can be configured with:

    dependencies {
      compile 'com.dslplatform:dsl-json:1.10.0'
      annotationProcessor 'com.dslplatform:dsl-json-processor:1.10.0'
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

Types without builtin mapping can be supported the same way as in Java8 version.
The main difference that converters only support the legacy format with `JSON_READER`/`JSON_WRITER`.

Custom converter for `java.util.Date` can be found in [example project](examples/MavenJava6/src/main/java/com/dslplatform/maven/Example.java#L116) 
Annotation processor will check if custom type implementations have appropriate signatures.
Converter for `java.util.ArrayList` can be found in [same example project](examples/MavenJava6/src/main/java/com/dslplatform/maven/Example.java#L38) 

`@JsonConverter` which implements `Configuration` will also be registered in `META-INF/services` which makes it convenient to [setup initialization](examples/MavenJava6/src/main/java/com/dslplatform/maven/ImmutablePerson.java#L48).

## Dependencies

Core library (with analysis processor) and DSL Platform annotation processor targets Java6.
Java8 library includes runtime analysis, reflection support, annotation processor and Java8 specific types. When Java8 annotation processor is used Mono/.NET doesn't need to be present on the system.
Android can use Java8 version of the library even on older versions due to lazy loading of types which avoids loading types Android does not support. 

If not sure which version to use, use Java8 version of the library with annotation processor.

## FAQ

 ***Q***: DslJson is failing with unable to resolve reader/writer. What does it mean?  
 ***A***: During startup DslJson loads services through `ServiceLoader`. For this to work `META-INF/services/com.dslplatform.json.Configuration` must exist with the content of `dsl_json_Annotation_Processor_External_Serialization` or `dsl_json.json.ExternalSerialization` which is the class crated during compilation step. Make sure you've referenced processor library (which is responsible for setting up readers/writers during compilation) and double check if annotation processor is running. Refer to [example projects](examples) for how to set up environment. As of v1.8.0 Java8 version of the library avoids this issue since services are not used by default anymore in favor of named based convention. Eclipse is known to create problems with annotation processor since it requires manual setup (instead of using pom.xml setup). For Eclipse the best workaround is to build with Maven instead or relying on its build tools.

 ***Q***: Maven/Gradle are failing during compilation with `@CompiledJson` when I'm using DSL Platform annotation processor. What can I do about it?  
 ***A***: If Mono/.NET is available it *should* work out-of-the-box. But if some strange issue occurs, detailed log can be enabled to see what is causing the issue. Log is disabled by default, since some Gradle setups fail if something is logged during compilation. Log can be enabled with `dsljson.loglevel` [processor option](examples/MavenJava6/pom.xml#L35)

 ***Q***: DSL Platform annotation processor checks for new DSL compiler version on every compilation. How can I disable that?  
 ***A***: If you specify custom `dsljson.compiler` processor option or put `dsl-compiler.exe` in project root it will use that one and will not check online for updates
 
 ***Q***: What is this DSL Platform?  
 ***A***: DSL Platform is a proprietary compiler written in C#. Since v1.7.0 DSL Platform is no longer required to create compile-time databinding. Compiler is free to use, but access to source code is licensed. If you want access to the compiler or need performance consulting [let us know](https://dsl-platform.com)
