package com.dslplatform.json.processor;

public enum LogLevel {
	DEBUG(0),
	INFO(1),
	ERRORS(2),
	NONE(3);

	private final int level;

	LogLevel(int level) {
		this.level = level;
	}

	public boolean isVisible(LogLevel other) {
		return other.level <= this.level;
	}
}
