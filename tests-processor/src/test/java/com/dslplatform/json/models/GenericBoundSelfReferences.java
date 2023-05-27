package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public abstract class GenericBoundSelfReferences {
	@CompiledJson
	public static class GenericBoundSelfReferenceString extends GenericBoundSelfReference<String> {
		public GenericBoundSelfReferenceString(String stringValue) {
			super(stringValue);
		}
	}

	@CompiledJson
	public static class GenericBoundSelfReference<T> implements Comparable<GenericBoundSelfReference<T>> {

		final T genericField;

		public GenericBoundSelfReference(T genericField) {
			this.genericField = genericField;
		}

		public final T getGenericField() {
			return genericField;
		}

		@Override
		public int compareTo(GenericBoundSelfReference<T> other) {
			return 1;
		}
	}
}