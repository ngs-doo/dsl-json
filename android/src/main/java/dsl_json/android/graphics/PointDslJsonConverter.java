package dsl_json.android.graphics;

import com.dslplatform.json.AndroidGeomConverter;
import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;

public class PointDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(android.graphics.Point.class, AndroidGeomConverter.POINT_READER);
		json.registerWriter(android.graphics.Point.class, AndroidGeomConverter.POINT_WRITER);
	}
}