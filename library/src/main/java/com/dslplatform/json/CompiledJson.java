package com.dslplatform.json;

import java.lang.annotation.*;

/**
 * Compile time data-binding annotation.
 * Objects with this annotation will have their serializers/deserializers created during project compilation.
 * They will be registered into META-INF/services/com.dslplatform.json.CompiledJson and should be loaded during
 * DslJson initialization.
 * <p>
 * If classes with this annotation reference another class which doesn't have this annotation, annotation processor
 * will behave as they have @CompiledJson annotation (this can be controlled via compiler option).
 * This can be used to create converters for objects which can't be modified.
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface CompiledJson {
	/**
	 * JSON can be encoded/decoded in several ways:<br>
	 * 	- object - standard attribute: value pair, eg: {"prop":123,"attr":"abc"}<br>
	 * 	- array - no attributes, just values in array, eg: [123,"abc"]<br>
	 * <p>
	 * Array format [prop1, prop2, prop3] is useful when sending known format on both sides since
	 * it assumes fixed property order and all properties must be known by both sides.
	 * index() parameter can be used to control the ordering.
	 * <p>
	 * Serialization will be done via the first available format.
	 *
	 * @return JSON format
	 */
	Format[] formats() default {Format.OBJECT};

	enum Format {
		OBJECT,
		ARRAY
	}

	/**
	 * JSON attribute names can be minified using builtin simplistic algorithm
	 * which results in smaller JSON and faster processing.
	 * This is useful when using JSON for persistence instead of public API or Javascript interoperability.
	 *
	 * @return should JSON properties use short names
	 */
	boolean minified() default false;

	/**
	 * Specify how to handle unknown property during object processing.
	 * On classes, default behavior is to skip over it and go to the next property.
	 * In enums, default behavior is to fail instead of defaulting to first value.
	 * Fail will result in an IOException, while skip will continue processing JSON.
	 *
	 * @return should skip unknown properties
	 */
	Behavior onUnknown() default Behavior.DEFAULT;

	enum Behavior {
		DEFAULT,
		FAIL,
		IGNORE
	}

	/**
	 * Abstract types used as properties by default include type signature information so they can be properly deserialized.
	 * Type signature is included with as additional "$type":"actual.type.name" property (at the start of the object).
	 * To disable inclusion of $type attribute, set this property to EXCLUDE.
	 *
	 * @return should include type signature in JSON
	 */
	TypeSignature typeSignature() default TypeSignature.DEFAULT;

	enum TypeSignature {
		DEFAULT,
		EXCLUDE
	}

	/**
	 * Perform deserialization into a different type.
	 * For abstract types it's often useful to specify a concrete implementation for deserialization.
	 * This is required if $type attribute is missing from the JSON.
	 *
	 * @return deserialize into a specified signature
	 */
	Class deserializeAs() default CompiledJson.class;

	/**
	 * When used in mixin which doesn't have deserializeAs "$type":name will be injected into JSON.
	 * If not specified name will be the class full name, otherwise it will use value provided here.
	 * Value must be unique across all mixin implementations.
	 *
	 * @return deserialization hint
	 */
	String deserializeName() default "";
}