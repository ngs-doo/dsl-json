package com.dslplatform.json;

import java.lang.annotation.*;

/**
 * Compile time data-binding annotation.
 * Objects with this annotation will have their serializers/deserializers created during project compilation.
 * They will be registered into META-INF/services/com.dslplatform.json.CompiledJson and should be loaded during
 * DslJson initialization.
 *
 * If classes with this annotation reference another class which doesn't have this annotation, annotation processor
 * will behave as they have @CompiledJson annotation.
 * This can be used to create converters for objects which can't be modified.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CompiledJson {
	/**
	 * JSON attribute names can be minified using builtin simplistic algorithm
	 * which results in smaller JSON and faster processing.
	 * This is useful when using JSON for persistance instead of public API or Javascript interoperability.
	 *
	 * @return should JSON properties use short names
	 */
	boolean minified() default false;
}