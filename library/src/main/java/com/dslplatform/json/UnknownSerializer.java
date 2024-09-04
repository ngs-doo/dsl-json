package com.dslplatform.json;

import java.io.IOException;

public interface UnknownSerializer {
	void serialize(JsonWriter writer, @Nullable Object unknown) throws IOException;
}
