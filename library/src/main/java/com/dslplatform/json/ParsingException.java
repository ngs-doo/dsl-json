package com.dslplatform.json;

import java.io.IOException;

public class ParsingException extends IOException {
	public ParsingException(String reason) {
		super(reason);
	}

	public ParsingException(Throwable cause) {
		super(cause);
	}

	public ParsingException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
