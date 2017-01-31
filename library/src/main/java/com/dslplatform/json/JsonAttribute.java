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
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface JsonAttribute {
	/**
	 * Should this property be read/written in JSON.
	 *
	 * @return true for ignored property
	 */
	boolean ignore() default false;

	/**
	 * When mandatory is enabled, property will always be written in JSON.
	 * If property is missing during parsing an IOException will be thrown.
	 *
	 * @return true for ignored property
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
}