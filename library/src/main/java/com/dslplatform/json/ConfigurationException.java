package com.dslplatform.json;

/**
 * DSL-JSON specific exception thrown when DSL-JSON was not configured for types
 * which are attempted to be serialized.
 *
 * Even if runtime analysis is enabled, if type is specific, it could require
 * specialized converter. If such converter is not registered this exception will
 * be thrown due to "faulty" DSL-JSON configuration.
 */
public class ConfigurationException extends RuntimeException {
	public ConfigurationException(String reason) {
		super(reason);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
