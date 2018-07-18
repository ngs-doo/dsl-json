package dsl_json.android.graphics;

import com.dslplatform.json.AndroidGeomConverter;
import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;

public class RectDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(android.graphics.Rect.class, AndroidGeomConverter.RECTANGLE_READER);
		json.registerWriter(android.graphics.Rect.class, AndroidGeomConverter.RECTANGLE_WRITER);
	}
}