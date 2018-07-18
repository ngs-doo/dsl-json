package com.dslplatform.json;

import dsl_json.android.graphics.*;
import org.w3c.dom.Element;

public class ConfigureAndroid implements Configuration {
	@Override
	public void configure(DslJson json) {
		new PointFDslJsonConverter().configure(json);
		new PointDslJsonConverter().configure(json);
		new RectDslJsonConverter().configure(json);
		new BitmapDslJsonConverter().configure(json);
		json.registerReader(Element.class, XmlConverter.Reader);
		json.registerWriter(Element.class, XmlConverter.Writer);
	}
}
