package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON converter can be specified for class.
 * It must have JSON_READER and JSON_WRITER which do the JSON conversion.
 * This can be used for fine-tuning the serialization/deserialization process.
 *
 * Eg. java.time.Instant is not a supported type. After creating class such as
 *
 * <pre>
 *     {@literal @}JsonConverter(target=Instant.class)
 *     public abstract InstantConverter {
 *         public static Instant read(JsonReader reader) { ... }
 *         public static void write(JsonWriter writer, Instant value) { ... }
 *         public static Instant default() { ... }
 *     }
 * </pre>
 *
 * Date type will be one of the supported types.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface JsonConverter {
	/**
	 * For which class this converter applies.
	 *
	 * @return target type
	 */
	Class target();
}