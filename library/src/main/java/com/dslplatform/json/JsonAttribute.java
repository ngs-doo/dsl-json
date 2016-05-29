package com.dslplatform.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface JsonAttribute {
	boolean ignore() default false;
	boolean nullable() default true;
	String name() default "";
	boolean hashMatch() default true;
	String[] alternativeNames() default {};
}