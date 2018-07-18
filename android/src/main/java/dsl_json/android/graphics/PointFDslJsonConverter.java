package dsl_json.android.graphics;

import com.dslplatform.json.AndroidGeomConverter;
import com.dslplatform.json.Configuration;
import com.dslplatform.json.DslJson;

public class PointFDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(android.graphics.PointF.class, AndroidGeomConverter.LOCATION_READER);
		json.registerWriter(android.graphics.PointF.class, AndroidGeomConverter.LOCATION_WRITER);
	}
}