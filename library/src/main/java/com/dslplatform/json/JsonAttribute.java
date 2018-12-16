package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile time property configuration.
 * Specify custom property for processing specific properties.
 * Eg. different property name in JSON, ignore this property, etc...
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface JsonAttribute {
	/**
	 * Should this property be read/written in JSON.
	 *
	 * @return true for ignored property
	 */
	boolean ignore() default false;

	/**
	 * When mandatory is enabled, property must always exist in incoming JSON.
	 * If property is missing during parsing an IOException will be thrown.<br>
	 * Mandatory does not mean property will always be included in JSON,
	 * but this can be enforced either by using full serialization on object format,
	 * array format, or includeToMinimal setting for minimal serialization.<br>
	 *
	 * @return true for mandatory property
	 */
	boolean mandatory() default false;

	/**
	 * Can this field be nullable.
	 * This will omit null checks for micro performance boost (reduced branching).
	 *
	 * @return check for null values
	 */
	boolean nullable() default true;

	/**
	 * Property name in JSON. When property name in JSON differs from this property name.
	 * Eg. "my_property" for public String myProperty;
	 *
	 * @return JSON property name
	 */
	String name() default "";


	/**
	 * Specify index order during serialization.
	 * -1 implies default order
	 *
	 * @return order when defined
	 */
	int index() default -1;

	/**
	 * Use only hash match for detecting incoming property.
	 * DSL-JSON by default matches properties only by hash value.
	 * If object contains multiple properties with same hash value additional checks are performed.
	 * Those checks can be enabled always by setting hashMatch to false.
	 *
	 * @return should only hashMatch be used
	 */
	boolean hashMatch() default true;

	/**
	 * Multiple different incoming JSON properties can be deserialized into same POJO property.
	 * This is useful during model changes or for coping with casing issues.
	 *
	 * Eg. "my_property", "myProperty", "MyProperty" can all be deserialized into public String myProperty;
	 *
	 * @return alternative JSON property names
	 */
	String[] alternativeNames() default {};

	/**
	 * Custom property converter.
	 * Unlike {@literal @}JsonConverter which specifies converter for type, this converter specifies
	 * conversion only for this property.
	 * This can be used for simple formatting or for coping with "invalid" JSON.
	 *
	 * @return converter used for this property
	 */
	Class converter() default JsonAttribute.class;

	/**
	 * Abstract types used as properties by default include type signature information so they can be properly deserialized.
	 * Type signature is included with as additional "$type":"actual.type.name" property (at the start of the object).
	 * To disable inclusion of $type attribute, set this property to EXCLUDE.
	 * This property overrides the value set on @CompiledJson
	 *
	 * @return should include type signature in JSON
	 */
	CompiledJson.TypeSignature typeSignature() default CompiledJson.TypeSignature.DEFAULT;

	/**
	 * Allows for fine tuning minimal serialization object format.
	 * By default only properties with non default values will be included during serialization for such configuration.
	 * To always include some properties in output JSON this settings can be set to ALWAYS.<br>
	 * While mandatory will require that property exists during parsing,
	 * this complements the feature so that property always exist in output JSON.
	 *
	 * @return minimal serialization object format policy for this property
	 */
	IncludePolicy includeToMinimal() default IncludePolicy.NON_DEFAULT;

	/**
	 * Customize property serialization behavior in minimal serialization object format.
	 */
	enum IncludePolicy {
		/**
		 * Include property only if the value is non default. Eg, non 0 for int, non null for references
		 */
		NON_DEFAULT,
		/**
		 * Always include property in the output
		 */
		ALWAYS
	}
}