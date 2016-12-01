package com.dslplatform.json.generated.ocd.test;

import org.w3c.dom.Element;

public abstract class Utils {
	public static Element stringToElement(final String element) {
		try {
			return XMLConverter.INSTANCE.stringToDocument(element).getDocumentElement();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String elementToString(final Element element) {
		try {
			return XMLConverter.INSTANCE.nodeToString(element);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
