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
	private final Context context;

	EnumTemplate(Context context) {
		this.code = context.code;
		this.context = context;
	}

	private static boolean isAllSimple(StructInfo si) {
		for (String c : si.constants) {
			if (!c.matches("\\w+")) return false;
		}
		return true;
	}

	void writeName(Writer code, StructInfo target, String readValue, String writerName) throws IOException {
		writeName(code, target, readValue, writerName, true);
	}
	private void writeName(Writer code, StructInfo target, String readValue, String writerName, boolean external) throws IOException {
		if (target.enumConstantNameSource != null) {
			String constantNameType = extractReturnType(target.enumConstantNameSource);
			StructInfo info = context.structs.get(constantNameType);
			OptimizedConverter converter = context.inlinedConverters.get(constantNameType);
			String value = readValue + "." + target.enumConstantNameSource;
			if (info != null && info.converter != null) {
				code.append(info.converter.fullName).append(".").append(info.converter.writer).append(".write(writer, ").append(value).append(");\n");
			} else if (converter != null) {
				code.append(converter.nonNullableEncoder("writer", value)).append(";\n");
			} else {
				code.append(writerName).append(".write(writer, ").append(external ? readValue : value).append(");\n");
			}
		} else if (isAllSimple(target)) {
			code.append("{ writer.writeByte((byte)'\"'); writer.writeAscii(").append(readValue).append(".name()); writer.writeByte((byte)'\"'); }\n");
		} else {
			code.append("writer.writeString(value.name());\n");
		}
	}

	boolean isStatic(final StructInfo si) {
		if (si.enumConstantNameSource == null) return true;
		String constantNameType = extractReturnType(si.enumConstantNameSource);
		if (constantNameType == null) return true;
		StructInfo info = context.structs.get(constantNameType);
		return info != null && info.converter != null
				|| context.inlinedConverters.get(constantNameType) != null;
	}

	void create(final StructInfo si, final String className) throws IOException {
		code.append("\tpublic final static class EnumConverter implements com.dslplatform.json.JsonWriter.WriteObject<");
		code.append(className);
		code.append(">, com.dslplatform.json.JsonReader.ReadObject<").append(className).append("> {\n");
		String constantNameType = extractReturnType(si.enumConstantNameSource);
		StructInfo info = constantNameType != null ? context.structs.get(constantNameType) : null;
		OptimizedConverter optimizedConverter = constantNameType != null ? context.inlinedConverters.get(constantNameType) : null;
		if (constantNameType != null) {
			code.append("\t\tprivate static final java.util.Map<").append(constantNameType).append(", ").append(className).append("> values;\n");
			code.append("\t\tstatic {\n");
			code.append("\t\t\tvalues = new java.util.HashMap<").append(constantNameType).append(", ").append(className).append(">();\n");
			code.append("\t\t\tfor(").append(className).append(" value : ").append(className).append(".values()) {\n");
			code.append("\t\t\t\tvalues.put(value.").append(si.enumConstantNameSource.toString()).append(", value);\n");
			code.append("\t\t\t}\n");
			code.append("\t\t}\n");
			if (optimizedConverter == null && (info == null || info.converter == null)) {
				code.append("\t\tprivate final com.dslplatform.json.JsonWriter.WriteObject<").append(constantNameType).append("> valueWriter;\n");
				code.append("\t\tprivate final com.dslplatform.json.JsonReader.ReadObject<").append(constantNameType).append("> valueReader;\n");
				code.append("\t\tpublic EnumConverter(com.dslplatform.json.DslJson<Object> __dsljson) {\n");
				code.append("\t\t\tthis.valueWriter = __dsljson.tryFindWriter(").append(Context.typeOrClass(constantNameType, constantNameType)).append(");\n");
				code.append("\t\t\tif (this.valueWriter == null) throw new com.dslplatform.json.SerializationException(\"Unable to find writer for ").append(constantNameType).append("\");\n");
				code.append("\t\t\tthis.valueReader = __dsljson.tryFindReader(").append(Context.typeOrClass(constantNameType, constantNameType)).append(");\n");
				code.append("\t\t\tif (this.valueReader == null) throw new com.dslplatform.json.SerializationException(\"Unable to find reader for ").append(constantNameType).append("\");\n");
				code.append("\t\t}\n");
			}
		}
		code.append("\t\tpublic void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" value) {\n");
		code.append("\t\t\tif (value == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		code.append("\t\t\t\t");
		writeName(code, si, "value", "valueWriter", false);
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.wasNull()) return null;\n");
		if (isStatic(si)) {
			code.append("\t\t\treturn readStatic(reader);\n");
			code.append("\t\t}\n");
			code.append("\t\tpublic static ").append(className).append(" readStatic(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		}
		if (constantNameType != null) {
			if (info != null && info.converter != null) {
				code.append("\t\t\tfinal ").append(constantNameType).append(" input = ").append(info.converter.fullName).append(".").append(info.converter.reader).append(".read(reader);\n");
				code.append("\t\t\t").append(className).append(" value = ").append("values.get(input);\n");
			} else if (optimizedConverter != null) {
				code.append("\t\t\tfinal ").append(constantNameType).append(" input = ").append(optimizedConverter.nonNullableDecoder()).append("(reader);\n");
				code.append("\t\t\t").append(className).append(" value = ").append("values.get(input);\n");
			} else {
				code.append("\t\t\tfinal ").append(constantNameType).append(" input = valueReader.read(reader);\n");
				code.append("\t\t\t").append(className).append(" value = input == null ? null : ").append("values.get(input);\n");
			}
			code.append("\t\t\tif (value == null) {\n");
			if (si.onUnknown == CompiledJson.Behavior.IGNORE) {
				code.append("\t\t\t\tvalue = ").append(className).append(".").append(si.constants.get(0)).append(";\n");
			} else {
				code.append("\t\t\t\tthrow new java.lang.IllegalArgumentException(\"No enum constant ");
				code.append(className).append(" associated with value '\" + input + \"'");
				if (info != null && info.converter != null) {
					code.append(". When using custom objects check that custom hashCode and equals are implemented");
				}
				code.append("\");\n");
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
	private static String extractReturnType(@Nullable Element element) {
		if (element == null) return null;
		switch (element.getKind()) {
			case FIELD: return Analysis.objectName(element.asType().toString());
			case METHOD: return Analysis.objectName(((ExecutableElement) element).getReturnType().toString());
			default: return null;
		}
	}
}
