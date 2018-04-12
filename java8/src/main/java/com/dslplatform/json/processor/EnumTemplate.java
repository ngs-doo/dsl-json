package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

class EnumTemplate {

	private final Writer code;

	EnumTemplate(Context context) {
		this.code = context.code;
	}

	private static boolean isAllSimple(StructInfo si) {
		for (String c : si.constants) {
			if (!c.matches("\\w+")) return false;
		}
		return true;
	}

	static void writeName(Context context, AttributeInfo attr, String readValue) throws IOException {
		StructInfo target = context.structs.get(attr.targetName);
		if (isAllSimple(target)) {
			context.code.append("{ writer.writeByte((byte)'\"'); writer.writeAscii(").append(readValue).append(".name()); writer.writeByte((byte)'\"'); }");
		} else {
			context.code.append("writer.writeString(value.name());\n");
		}
	}

	void create(final StructInfo si, final String className) throws IOException {
		Set<Integer> hashCodes = new HashSet<>();
		for(String c : si.constants) {
			hashCodes.add(StructInfo.calcHash(c));
		}
		boolean allSimple = isAllSimple(si);
		code.append("\tfinal static class Enum_").append(si.name);
		code.append(" implements com.dslplatform.json.JsonWriter.WriteObject<").append(className);
		code.append(">, com.dslplatform.json.JsonReader.ReadObject<").append(className).append("> {\n");
		code.append("\t\tpublic void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" value) {\n");
		code.append("\t\t\tif (value == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		if (allSimple) code.append("\t\t\t\twriter.writeByte((byte)'\"'); writer.writeAscii(value.name()); writer.writeByte((byte)'\"');\n");
		else code.append("\t\t\t\twriter.writeString(value.name());\n");
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\treturn reader.wasNull() ? null : readStatic(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tstatic ").append(className).append(" readStatic(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		if (hashCodes.size() == si.constants.size()) {
			code.append("\t\t\tswitch (reader.calcHash()) {\n");
			for(String c : si.constants) {
				int hash = StructInfo.calcHash(c);
				code.append("\t\t\t\tcase ").append(Integer.toString(hash)).append(":\n");
				code.append("\t\t\t\t\treturn ").append(className).append(".").append(c).append(";\n");
			}
			code.append("\t\t\t\tdefault:\n");
			if (si.onUnknown == CompiledJson.Behavior.IGNORE) {
				code.append("\t\t\t\t\treturn ").append(className).append(".").append(si.constants.get(0)).append(";\n");
			} else {
				code.append("\t\t\t\t\treturn ").append(className).append(".valueOf(reader.getLastName());\n");
			}
			code.append("\t\t\t}\n");
			//TODO: better handle for collision
		} else code.append("\t\t\treturn ").append(className).append(".valueOf(reader.getLastName());\n");
		code.append("\t\t}\n");
		code.append("\t}\n");
	}
}
