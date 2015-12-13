package com.dslplatform.json.generated.ocd.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public enum XMLConverter {
	INSTANCE;

	private final ThreadLocal<DocumentBuilder> documentBuilder = new ThreadLocal<DocumentBuilder>() {
		@Override
		protected DocumentBuilder initialValue() {
			try {
				final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setValidating(false);
				dbf.setFeature("http://xml.org/sax/features/namespaces", false);
				dbf.setFeature("http://xml.org/sax/features/validation", false);
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				dbf.setNamespaceAware(false);
				dbf.newDocumentBuilder();
				return dbf.newDocumentBuilder();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	};

	private final ThreadLocal<Transformer> transformer = new ThreadLocal<Transformer>() {
		@Override
		protected Transformer initialValue() {
			try {
				final TransformerFactory tf = TransformerFactory.newInstance();
				final Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.INDENT, "no");
				return transformer;
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	};

	/**
	 * Converts a <code>String</code> into a <code>org.w3c.dom.Document</code>.
	 * Use a <code>ThreadLocal</code> instance of the
	 * <code>DocumentBuilderFactory</code> to prevent the FWK005 parsing while
	 * parsing exception when being used by concurrent threads.
	 *
	 * @throws IOException
	 * @throws SAXException
	 */
	public Document stringToDocument(final String xml) throws SAXException, IOException {
		return documentBuilder.get().parse(new InputSource(new StringReader(xml)));
	}

	/**
	 * Converts a <code>org.w3c.dom.Node</code> into a <code>String</code> Use a
	 * <code>ThreadLocal</code> instance of the <code>Transformer</code> to
	 * prevent potential concurrency exceptions.
	 *
	 * @throws TransformerException
	 */
	public String nodeToString(final Node node) throws TransformerException {
		final StringWriter sw = new StringWriter();
		transformer.get().transform(new DOMSource(node), new StreamResult(sw));
		return sw.toString();
	}
}
