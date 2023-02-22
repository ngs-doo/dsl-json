package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define a default value
 * used when trying to deserialize unknown Enum values.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface JsonEnumDefaultValue {
}
