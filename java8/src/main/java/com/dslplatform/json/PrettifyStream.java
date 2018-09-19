package com.dslplatform.json;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PrettifyStream {

	private String ident = "  ";
	private String nl = "\n";

	public PrettifyStream() {
		this("  ", "\n");
	}

	public PrettifyStream(String ident, String nl) {
		if (ident == null) throw new IllegalArgumentException("ident can't be null");
		if (nl == null) throw new IllegalArgumentException("nl can't be null");
		this.ident = ident;
		this.nl = nl;
	}

	public String process(String input) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		process(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), os);
		return os.toString("UTF-8");
	}

	public void process(InputStream source, OutputStream target) throws IOException {
		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(source), StandardCharsets.UTF_8);
		OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(target), StandardCharsets.UTF_8);
		process(reader, writer);
	}

	public void process(Reader reader, Writer writer) throws IOException {
		int level = 0;
		boolean inString = false;
		boolean inEscape = false;
		int last;
		boolean hasElements = false;
		while ((last = reader.read()) != -1) {
			if (inString) {
				writer.write(last);
				if (last == '"' && !inEscape) {
					inString = false;
				}
				inEscape = !inEscape && last == '\\';
			} else if (last == '[' || last == '{') {
				if (!hasElements && level != 0) {
					writeNewRow(writer, level);
				}
				level++;
				writer.write(last);
				hasElements = false;
			} else if (last == ',') {
				writer.write(",");
				writeNewRow(writer, level);
				hasElements = true;
			} else if (last == '\"') {
				if (!hasElements && level != 0) {
					writeNewRow(writer, level);
				}
				writer.write("\"");
				inString = true;
				inEscape = false;
				hasElements = true;
			} else if (last == '}' || last == ']') {
				level--;
				if (hasElements) {
					writeNewRow(writer, level);
				}
				writer.write(last);
				hasElements = true;
			} else if (last == ':') {
				writer.write(": ");
				hasElements = true;
			} else if (!Character.isWhitespace(last)) {
				if (!hasElements && level != 0) {
					writeNewRow(writer, level);
				}
				writer.write(last);
				hasElements = true;
			}
		}
		writer.flush();
	}

	private void writeNewRow(Writer writer, int level) throws IOException {
		writer.write(nl);
		for (int i = 0; i < level; i++) {
			writer.write(ident);
		}
	}
}
