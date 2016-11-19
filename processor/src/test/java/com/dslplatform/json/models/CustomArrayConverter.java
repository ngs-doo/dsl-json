package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.ArrayList;

@CompiledJson
public class CustomArrayConverter {
	@JsonAttribute(converter = FormatList.class)
	public ArrayList<Integer> list;

	public static abstract class FormatList {
		public static final JsonReader.ReadObject<ArrayList<Integer>> JSON_READER = new JsonReader.ReadObject<ArrayList<Integer>>() {
			public ArrayList<Integer> read(JsonReader reader) throws IOException {
				reader.getNextToken();
				return new ArrayList<Integer>();
			}
		};
		public static final JsonWriter.WriteObject<ArrayList<Integer>> JSON_WRITER = new JsonWriter.WriteObject<ArrayList<Integer>>() {
			public void write(JsonWriter writer, ArrayList<Integer> value) {
				writer.writeAscii("[]");
			}
		};
	}
}
