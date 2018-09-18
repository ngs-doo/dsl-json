package com.dslplatform.json;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

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
		Stack<Character> nesting = new Stack<>();
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
				if (!hasElements && !nesting.isEmpty()) {
					writeNewRow(writer, nesting);
				}
				nesting.push((char)last);
				writer.write(last);
				hasElements = false;
			} else if (last == 't') {
				if (reader.read() != 'r'
						|| reader.read() != 'u'
						|| reader.read() != 'e') {
					throw new IOException("Invalid JSON provided. Expecting true");
				}
				if (!hasElements && !nesting.isEmpty()) {
					writeNewRow(writer, nesting);
				}
				writer.write("true");
				hasElements = true;
			} else if (last == 'f') {
				if (reader.read() != 'a'
						|| reader.read() != 'l'
						|| reader.read() != 's'
						|| reader.read() != 'e') {
					throw new IOException("Invalid JSON provided. Expecting false");
				}
				if (!hasElements && !nesting.isEmpty()) {
					writeNewRow(writer, nesting);
				}
				writer.write("false");
				hasElements = true;
			} else if (last == ',') {
				writer.write(",");
				writeNewRow(writer, nesting);
				hasElements = true;
			} else if (last == '\"') {
				if (!hasElements && !nesting.isEmpty()) {
					writeNewRow(writer, nesting);
				}
				writer.write("\"");
				inString = true;
				inEscape = false;
				hasElements = true;
			} else if (last == '}' || last == ']') {
				nesting.pop();
				if (hasElements) {
					writeNewRow(writer, nesting);
				}
				writer.write(last);
				hasElements = true;
			} else if (last == ':') {
				writer.write(": ");
				hasElements = true;
			} else if (!Character.isWhitespace(last)) {
				if (!hasElements && !nesting.isEmpty()) {
					writeNewRow(writer, nesting);
				}
				writer.write(last);
				hasElements = true;
			}
		}
		reader.close();
		writer.flush();
		writer.close();
	}

	private void writeNewRow(OutputStreamWriter writer, Stack<Character> nesting) throws IOException {
		writer.write(nl);
		for (int i = 0; i < nesting.size(); i++) {
			writer.write(ident);
		}
	}
}
