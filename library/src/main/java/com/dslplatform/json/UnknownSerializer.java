package com.dslplatform.json;

import java.io.IOException;

interface UnknownSerializer {
	void serialize(JsonWriter writer, @Nullable Object unknown) throws IOException;
}
