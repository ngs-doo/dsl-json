package com.dslplatform.json;

public class SerializationException extends RuntimeException {
	public SerializationException(String reason) {
		super(reason);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}
}
