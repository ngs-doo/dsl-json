package com.dslplatform.json;

import org.w3c.dom.Element;

public class ConfigureAndroid implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerReader(android.graphics.PointF.class, AndroidGeomConverter.LocationReader);
		json.registerWriter(android.graphics.PointF.class, AndroidGeomConverter.LocationWriter);
		json.registerReader(android.graphics.Point.class, AndroidGeomConverter.PointReader);
		json.registerWriter(android.graphics.Point.class, AndroidGeomConverter.PointWriter);
		json.registerReader(android.graphics.Rect.class, AndroidGeomConverter.RectangleReader);
		json.registerWriter(android.graphics.Rect.class, AndroidGeomConverter.RectangleWriter);
		json.registerReader(android.graphics.Bitmap.class, AndroidGeomConverter.ImageReader);
		json.registerWriter(android.graphics.Bitmap.class, AndroidGeomConverter.ImageWriter);
		json.registerReader(Element.class, XmlConverter.Reader);
		json.registerWriter(Element.class, XmlConverter.Writer);
	}
}
