package com.dslplatform.json;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CompiledJson {
	boolean minified() default false;
}