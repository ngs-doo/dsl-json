package com.dslplatform.json.processor;

import com.dslplatform.json.Nullable;

final class OptimizedConverter {
	public final String encoderField;
	private final String nonNullableEncoderMethod;
	public final String decoderField;
	private final String nonNullableDecoderMethod;
	@Nullable
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

	String nonNullableEncoder(String writer, String value) {
		if (nonNullableEncoderMethod != null) return nonNullableEncoderMethod + "(" + value + ", " + writer + ")";
		return encoderField + ".write(" + writer + ", " + value + ")";
	}

	boolean hasNonNullableMethod() {
		return nonNullableDecoderMethod != null;
	}

	String nonNullableDecoder() {
		return nonNullableDecoderMethod != null ? nonNullableDecoderMethod : decoderField + ".read";
	}
}
