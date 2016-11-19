package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON converter can be specified for class.
 * It must have JSON_READER and JSON_WRITER which do the JSON conversion.
 * This can be used for fine tuning the serialization/deserialization process.
 *
 * Eg. java.util.Date is not a supported type. After creating class such as
 *
 * <pre>
 *     {@literal @}JsonConverter(target=Date.class)
 *     public abstract DateConverter {
 *         public static JsonReader.ReadObject&lt;Date&gt; JSON_READER = ...
 *         public static JsonWriter.WriteObject&lt;Date&gt; JSON_WRITER = ...
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