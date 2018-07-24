package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

final class OptimizedConverter {
	private final String encoderField;
	private final String nonNullableEncoderMethod;
	private final String decoderField;
	private final String nonNullableDecoderMethod;
	final String defaultValue;

	OptimizedConverter(String converter, String encoderField, String nonNullableEncoderMethod, String decoderField) {
		this(converter, encoderField, nonNullableEncoderMethod, decoderField, null, null);
	}

	OptimizedConverter(String converter, String encoderField, @Nullable String nonNullableEncoderMethod, String decoderField, @Nullable String nonNullableDecoderMethod, @Nullable String defaultValue) {
		this.encoderField = converter + "." + encoderField;
		this.nonNullableEncoderMethod = nonNullableEncoderMethod != null ? converter + "." + nonNullableEncoderMethod : null;
		this.decoderField = converter + "." + decoderField;
		this.nonNullableDecoderMethod = nonNullableDecoderMethod != null ? converter + "." + nonNullableDecoderMethod : null;
		this.defaultValue = defaultValue;
	}

	String encoder(String name, boolean nonNull) {
		if (nonNull && defaultValue == null) {
			if (nonNullableEncoderMethod != null) {
				return "(wrt, v) -> { if (v == null) throw new com.dslplatform.json.SerializationException(\"Property '" + name + "' is not allowed to be null\"); return " + nonNullableEncoderMethod + "(v, wrt); }";
			}
			return "(wrt, v) -> { if (v == null) throw new com.dslplatform.json.SerializationException(\"Property '" + name + "' is not allowed to be null\"); return " + encoderField + "(wrt, v); }";
		}
		return encoderField;
	}

	String nonNullableEncoder(String writer, String value) {
		if (nonNullableEncoderMethod != null) return nonNullableEncoderMethod + "(" + value + ", " + writer + ")";
		return encoderField + ".write(" + writer + ", " + value + ")";
	}

	String decoder(String name, boolean nonNull) {
		if (nonNull && defaultValue == null) {
			return "rdr -> { if (rdr.wasNull()) throw new java.io.IOException(\"Property '" + name + "' is not allowed to be null\"); return " + nonNullableDecoderMethod + "(rdr); }";
		}
		return decoderField;
	}

	boolean hasNonNullableMethod() {
		return nonNullableDecoderMethod != null;
	}

	String nonNullableDecoder() {
		return nonNullableDecoderMethod != null ? nonNullableDecoderMethod : decoderField + ".read";
	}
}
