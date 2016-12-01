package com.dslplatform.json;

import java.io.IOException;

public interface KeyCache {
	String getKey(int hash, char[] chars, int len) throws IOException;
}
