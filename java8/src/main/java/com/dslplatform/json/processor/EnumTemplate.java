package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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

	static void writeName(final Writer code, final StructInfo target, final String readValue) throws IOException {
		if (target.enumConstantNameSource != null) {
			String enumConstantNameType = extractReturnType(target.enumConstantNameSource);
			if ("int".equals(enumConstantNameType)) {
				code.append("com.dslplatform.json.NumberConverter.serialize(");
				code.append(readValue).append(".").append(target.enumConstantNameSource.toString());
				code.append(", writer);\n");
			} else {
				code.append("writer.writeString(").append(readValue).append(".").append(target.enumConstantNameSource.toString()).append(");\n");
			}
		} else if (isAllSimple(target)) {
			code.append("writer.writeByte((byte)'\"'); writer.writeAscii(").append(readValue).append(".name()); writer.writeByte((byte)'\"');\n");
		} else {
			code.append("writer.writeString(value.name());\n");
		}
	}

	void create(final StructInfo si, final String className) throws IOException {
		code.append("\tpublic final static class EnumConverter implements com.dslplatform.json.JsonWriter.WriteObject<");
		code.append(className);
		code.append(">, com.dslplatform.json.JsonReader.ReadObject<").append(className).append("> {\n");
		if (si.enumConstantNameSource != null) {
			String enumConstantNameType = extractReturnType(si.enumConstantNameSource);
			if ("int".equals(enumConstantNameType)) {
			 	enumConstantNameType = "java.lang.Integer";
			}
			code.append("\t\tprivate static final java.util.Map<").append(enumConstantNameType).append(", ").append(className).append("> values;\n");
			code.append("\t\tstatic {\n");
			code.append("\t\t\tvalues = new java.util.HashMap<").append(enumConstantNameType).append(", ").append(className).append(">();\n");
			code.append("\t\t\tfor(").append(className).append(" value : ").append(className).append(".values()) {\n");
			code.append("\t\t\t\tvalues.put(value.").append(si.enumConstantNameSource.toString()).append(", value);\n");
			code.append("\t\t\t}\n");
			code.append("\t\t}\n");
		}
		code.append("\t\tpublic void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" value) {\n");
		code.append("\t\t\tif (value == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		code.append("\t\t\t\t");
		writeName(code, si, "value");
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\treturn reader.wasNull() ? null : readStatic(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic static ").append(className).append(" readStatic(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		if (si.enumConstantNameSource != null) {
			String enumConstantNameType = extractReturnType(si.enumConstantNameSource);
			if ("int".equals(enumConstantNameType)) {
				code.append("\t\t\tint valueName = com.dslplatform.json.NumberConverter.deserializeInt(reader);\n");
			} else {
				code.append("\t\t\tjava.lang.String valueName = reader.readString();\n");
			}
			code.append("\t\t\t").append(className).append(" value = ").append("values.get(valueName);\n");
			code.append("\t\t\tif (value == null) {\n");
			if (si.onUnknown == CompiledJson.Behavior.IGNORE) {
				code.append("\t\t\t\tvalue = ").append(className).append(".").append(si.constants.get(0)).append(";\n");
			} else {
				code.append("\t\t\t\tthrow new java.lang.IllegalArgumentException(\"No enum constant ");
				code.append(className).append(" associated with value '\" + valueName + \"'\");\n");
			}
			code.append("\t\t\t}\n");
			code.append("\t\t\treturn value;\n");
		} else {
			Set<Integer> hashCodes = new HashSet<>();
			for(String c : si.constants) {
				hashCodes.add(StructInfo.calcHash(c));
			}
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
		}
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	@Nullable
	private static String extractReturnType(Element element) {
		switch (element.getKind()) {
			case FIELD: return element.asType().toString();
			case METHOD: return ((ExecutableElement) element).getReturnType().toString();
			default: return null;
		}
	}
}
