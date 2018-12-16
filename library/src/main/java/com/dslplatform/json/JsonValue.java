package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify json value source used for serialization/deserialization.
 *
 * Can be used on field or getter.
 *
 * <pre>
 *     enum MyEnum {
 *         FIRST("a"),
 *         SECOND("b");
 *
 *        {@literal @JsonValue}
 *         public final String value;
 *
 *         MyEnum(String value) {
 *             this.value = value;
 *         }
 *     }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface JsonValue {
}