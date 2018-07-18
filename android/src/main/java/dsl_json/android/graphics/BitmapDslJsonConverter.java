package dsl_json.android.graphics;

import com.dslplatform.json.AndroidGeomConverter;
import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;

public class BitmapDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(android.graphics.Bitmap.class, AndroidGeomConverter.IMAGE_READER);
		json.registerWriter(android.graphics.Bitmap.class, AndroidGeomConverter.IMAGE_WRITER);
	}
}