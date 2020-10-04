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
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
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

	/**
	 * JSON object format.
	 */
	enum Format {
		/**
		 * Standard format which includes both name and value, eg: {"number":123,"name":"json"}
		 */
		OBJECT,
		/**
		 * Compact format without attribute names, eg: [123,"json"]
		 */
		ARRAY
	}

	/**
	 * Object format can be fine tuned to either:<br>
	 *  - always include all properties<br>
	 *  - serialize only some properties<br>
	 *  <p>
	 *  This can be defined per DslJson instance via omitDefaults property during initialization through: skipDefaultValues<br>
	 *  To fine tune the behavior on class level specific policy can be set on the class which will override the global setting.<br>
	 *  <p>
	 *  By default minimal serialization will only include properties which have non-default values.
	 *  If some properties need to be always included regardless, this can be done via additional annotation on property level.
	 *
	 * @return object format policy
	 */
	ObjectFormatPolicy objectFormatPolicy() default ObjectFormatPolicy.DEFAULT;

	/**
	 * Class level tuning for object format serialization behavior.
	 * DEFAULT will inherit the runtime setting while other ones will always serialize object in
	 * specific way regardless of the runtime setting
	 */
	enum ObjectFormatPolicy {
		/**
		 * Inherit the serialization policy from DslJson omitDefaults setting
		 */
		DEFAULT,
		/**
		 * Serialize minimal set of properties (only required ones) regardless of DslJson omitDefaults setting
		 */
		MINIMAL,
		/**
		 * Always serialize all properties regardless of DslJson omitDefaults setting
		 */
		FULL,
		/**
		 * Serialize only properties marked with annotations.
		 * It will respect the omitDefault setting and serialize them accordingly.
		 */
		EXPLICIT
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

	/**
	 * Defines behavior for handling unknown properties.
	 * Enums fail by default, while class ignores unknown properties by default.
	 */
	enum Behavior {
		/**
		 * Use the object type default behavior
		 */
		DEFAULT,
		/**
		 * Always fail on unknown property
		 */
		FAIL,
		/**
		 * Cope with unknown properties somehow.
		 * In enums, first enum value will be used when an unknown value is parsed
		 */
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

	/**
	 * Some types (abstract classes and interfaces) require additional metadata so they can be correctly deserialized.
	 * This information can be excluded by using EXCLUDE type signature.
	 * An example of valid use case for exclusion is if there is some other way of handling deserialization, or if they are never deserialized at all.
	 *
	 */
	enum TypeSignature {
		/**
		 * Embed additional metadata as the first property during serialization
		 */
		DEFAULT,
		/**
		 * Exclude additional metadata from serialization of abstract classes and interfaces
		 */
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
	 * When used in mixin which doesn't have deserializeAs "discriminator":signature will be injected into JSON.
	 * If discriminator is not set, default value of $type will be used.
	 * When discriminator is used on class (in object format), additional property with name will be added to JSON,
	 * even when there is no underlying abstract class or interface.
	 *
	 * @return deserialization hint or additional serialization info
	 */
	String discriminator() default "";

	/**
	 * When used in mixin which doesn't have deserializeAs "$type":name will be injected into JSON.
	 * If not specified name will be the class full name, otherwise it will use value provided here.
	 * Value must be unique across all mixin implementations.
	 * To change default discriminator '$type' to something else, use discriminator value.
	 * When both discriminator and name are set on class, additional serialization info will be added to JSON,
	 * even when there is no underlying abstract class or interface.
	 *
	 * @return deserialization hint or additional serialization info
	 */
	String name() default "";
}