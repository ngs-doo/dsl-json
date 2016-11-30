package com.dslplatform.json;

import java.io.IOException;

interface KeyCache {
	String getKey(int hash, char[] chars, int len) throws IOException;
}
