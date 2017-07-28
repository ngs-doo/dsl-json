package com.dslplatform.json;

public class SerializationException extends RuntimeException {
	public SerializationException(String reason) {
		super(reason);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

	public SerializationException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
