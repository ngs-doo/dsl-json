package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class BuilderInfo {
	public final ExecutableElement factory;
	public final ExecutableElement ctor;
	public final TypeElement type;
	public final ExecutableElement build;
	public final AnnotationMirror annotation;

	public BuilderInfo(
			@Nullable ExecutableElement factory,
			@Nullable ExecutableElement ctor,
			TypeElement type,
			ExecutableElement build,
			@Nullable AnnotationMirror annotation) {
		this.factory = factory;
		this.ctor = ctor;
		this.type = type;
		this.build = build;
		this.annotation = annotation;
	}
}

