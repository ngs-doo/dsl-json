package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static com.dslplatform.json.processor.Context.nonGenericObject;
import static com.dslplatform.json.processor.Context.sortedAttributes;
import static com.dslplatform.json.processor.Context.typeOrClass;

class InlinedTemplate {

	private final Writer code;
	private final Context context;

	InlinedTemplate(Context context) {
		this.code = context.code;
		this.context = context;
	}

	private void asFormatConverter(final StructInfo si, final String name, final String className, final boolean binding) throws IOException {
		code.append("\tfinal static class ").append(name);
		code.append(" implements com.dslplatform.json.runtime.FormatConverter<");
		if (binding) {
			code.append(className).append(">, com.dslplatform.json.JsonReader.BindObject<");
		}
		code.append(className).append("> {\n");
		code.append("\t\tprivate final boolean alwaysSerialize;\n");
		code.append("\t\tprivate final com.dslplatform.json.DslJson json;\n");
		for (AttributeInfo attr : si.attributes.values()) {
			String typeName = attr.type.toString();
			boolean hasConverter = context.inlinedConverters.containsKey(typeName);
			if (attr.converter == null && !hasConverter && !attr.isEnum()) {
				String type = typeOrClass(nonGenericObject(typeName), typeName);
				code.append("\t\tprivate com.dslplatform.json.JsonReader.ReadObject<").append(typeName).append("> reader_").append(attr.name).append(";\n");
				code.append("\t\tprivate com.dslplatform.json.JsonReader.ReadObject<").append(typeName).append("> reader_").append(attr.name).append("() {\n");
				code.append("\t\t\tif (reader_").append(attr.name).append(" == null) { reader_").append(attr.name).append(" = json.tryFindReader(");
				code.append(type).append("); if (reader_").append(attr.name);
				code.append(" == null) throw new com.dslplatform.json.SerializationException(\"Unable to find reader for ").append(typeName).append("\"); }\n");
				code.append("\t\t\treturn reader_").append(attr.name).append(";\n");
				code.append("\t\t}\n");
				code.append("\t\tprivate com.dslplatform.json.JsonWriter.WriteObject<").append(typeName).append("> writer_").append(attr.name).append(";\n");
				code.append("\t\tprivate com.dslplatform.json.JsonWriter.WriteObject<").append(typeName).append("> writer_").append(attr.name).append("() {\n");
				code.append("\t\t\tif (writer_").append(attr.name).append(" == null) { writer_").append(attr.name).append(" = json.tryFindWriter(");
				code.append(type).append("); if (writer_").append(attr.name);
				code.append(" == null) throw new com.dslplatform.json.SerializationException(\"Unable to find writer for ").append(typeName).append("\"); }\n");
				code.append("\t\t\treturn writer_").append(attr.name).append(";\n");
				code.append("\t\t}\n");
			}
		}
		code.append("\t\t").append(name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\t\tthis.alwaysSerialize = !json.omitDefaults;\n");
		code.append("\t\t\tthis.json = json;\n");
		code.append("\t\t}\n");
		if (binding) {
			code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
			code.append("\t\t\tif (reader.wasNull()) return null;\n");
			code.append("\t\t\treturn bind(reader, new ").append(className).append("());\n");
			code.append("\t\t}\n");
		}
	}

	void emptyCtorObject(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si, "Object_" + si.name, className, true);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si);
		writeObject(className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" bind(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.last() != '{') throw new java.io.IOException(\"Expecting '{' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		code.append("\t\t\treader.getNextToken();\n");
		code.append("\t\t\tbindContent(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\t").append(className).append(" instance = new ").append(className).append("();\n");
		code.append("\t\t\tbindContent(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic void bindContent(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.last() == '}')");
		checkMandatory(sortedAttributes, 0);
		int i = 0;
		for (AttributeInfo attr : sortedAttributes) {
			String mn = si.minifiedNames.get(attr.id);
			if (i > 0) {
				code.append("\t\t\tif (reader.getNextToken() == '}') ");
				checkMandatory(sortedAttributes, i);
				code.append("\t\t\tif (reader.last() != ',') throw new java.io.IOException(\"Expecting ',' at position: \"");
				code.append(" + reader.positionInStream() + \". Found: \" + (char)reader.last()); else reader.getNextToken();\n");
			}
			code.append("\t\t\tif (reader.fillNameWeakHash() != ").append(Integer.toString(calcWeakHash(mn != null ? mn : attr.id)));
			code.append(" || !reader.wasLastName(name_").append(attr.name).append(")) { bindSlow(reader, instance, ");
			code.append(Integer.toString(i)).append("); return; }\n");
			code.append("\t\t\treader.getNextToken();\n");
			setPropertyValue(attr, "\t");
			i += 1;
		}
		if (si.onUnknown == CompiledJson.Behavior.FAIL) {
			code.append("\t\t\tif (reader.getNextToken() != '}') throw new java.io.IOException(\"Expecting '}' at position: \" + reader.positionInStream() + \" since unknown properties are not allowed on ");
			code.append(className).append(". Found \" + (char) reader.last());\n");
		} else {
			code.append("\t\t\tif (reader.getNextToken() != '}') {\n");
			code.append("\t\t\t\tif (reader.last() == ',') {\n");
			code.append("\t\t\t\t\treader.getNextToken();\n");
			code.append("\t\t\t\t\treader.fillNameWeakHash();\n");
			code.append("\t\t\t\t\tbindSlow(reader, instance, ").append(Integer.toString(sortedAttributes.size())).append(");\n");
			code.append("\t\t\t\t}\n");
			code.append("\t\t\t\tif (reader.last() != '}') throw new java.io.IOException(\"Expecting '}' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
			code.append("\t\t\t}\n");
		}
		code.append("\t\t}\n");
		code.append("\t\tprivate void bindSlow(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance, int index) throws java.io.IOException {\n");
		i = 0;
		for (AttributeInfo attr : sortedAttributes) {
			if (attr.mandatory) {
				code.append("\t\t\tboolean __detected_").append(attr.name).append("__ = index > ").append(Integer.toString(i)).append(";\n");
				i += 1;
			}
		}
		code.append("\t\t\tswitch(reader.getLastHash()) {\n");
		handleSwitch(si, "\t\t\t", false);
		code.append("\t\t\t}\n");
		code.append("\t\t\twhile (reader.last() == ','){\n");
		code.append("\t\t\t\treader.getNextToken();\n");
		code.append("\t\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t\t", false);
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\tif (reader.last() != '}') throw new java.io.IOException(\"Expecting '}' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		for (AttributeInfo attr : sortedAttributes) {
			if (attr.mandatory) {
				code.append("\t\t\tif (!__detected_").append(attr.name).append("__) throw new java.io.IOException(\"Property '").append(attr.name);
				code.append("' is mandatory but was not found in JSON at position: \" + reader.positionInStream());\n");
			}
		}
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	void fromCtorObject(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si, "Object_" + si.name, className, false);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si);
		writeObject(className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.wasNull()) return null;\n");
		code.append("\t\t\telse if (reader.last() != '{') throw new java.io.IOException(\"Expecting '{' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		code.append("\t\t\treader.getNextToken();\n");
		code.append("\t\t\treturn readContent(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		for (AttributeInfo attr : sortedAttributes) {
			String typeName = attr.type.toString();
			code.append("\t\t\t").append(typeName).append(" _").append(attr.name).append("_ = ");
			OptimizedConverter converter = context.inlinedConverters.get(typeName);
			if (converter == null || converter.defaultValue == null) code.append("null;\n");
			else code.append(converter.defaultValue).append(";\n");
			if (attr.mandatory) {
				code.append("\t\t\tboolean __detected_").append(attr.name).append("__ = false;\n");
			}
		}
		code.append("\t\t\tif (reader.last() == '}') {\n");
		checkMandatory(sortedAttributes);
		returnInstance("\t\t\t\t", si.constructor, className);
		code.append("\t\t\t}\n");
		code.append("\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t", true);
		code.append("\t\t\t}\n");
		code.append("\t\t\twhile (reader.last() == ','){\n");
		code.append("\t\t\t\treader.getNextToken();\n");
		code.append("\t\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t\t", true);
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\tif (reader.last() != '}') throw new java.io.IOException(\"Expecting '}' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		checkMandatory(sortedAttributes);
		returnInstance("\t\t\t", si.constructor, className);
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	private void writeObject(final String className, List<AttributeInfo> sortedAttributes) throws IOException {
		boolean isFirst = true;
		for (AttributeInfo attr : sortedAttributes) {
			String prefix = isFirst ? "" : ",";
			isFirst = false;
			code.append("\t\tprivate static final byte[] quoted_").append(attr.name).append(" = \"").append(prefix);
			code.append("\\\"").append(attr.id).append("\\\":\".getBytes(utf8);\n");
			code.append("\t\tprivate static final byte[] name_").append(attr.name).append(" = \"").append(attr.id).append("\".getBytes(utf8);\n");
		}
		code.append("\t\tpublic final void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		code.append("\t\t\tif (instance == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		code.append("\t\t\t\twriter.writeByte((byte)'{');\n");
		code.append("\t\t\t\tif (alwaysSerialize) { writeContentFull(writer, instance); writer.writeByte((byte)'}'); }\n");
		code.append("\t\t\t\telse if (writeContentMinimal(writer, instance)) writer.getByteBuffer()[writer.size() - 1] = '}';\n");
		code.append("\t\t\t\telse writer.writeByte((byte)'}');\n");
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic void writeContentFull(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\twriter.writeAscii(quoted_").append(attr.name).append(");\n");
			writeProperty(attr, false);
		}
		code.append("\t\t}\n");
		code.append("\t\tpublic boolean writeContentMinimal(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		code.append("\t\t\tboolean hasWritten = false;\n");
		for (AttributeInfo attr : sortedAttributes) {
			OptimizedConverter converter = context.inlinedConverters.get(attr.type.toString());
			String defaultValue = converter != null && converter.defaultValue != null ? converter.defaultValue : "null";
			code.append("\t\t\tif (instance.");
			code.append(attr.readProperty);
			code.append(" != ").append(defaultValue).append(") {\n");
			code.append("\t\t\t\twriter.writeByte((byte)'\"'); writer.writeAscii(name_").append(attr.name).append("); writer.writeByte((byte)'\"'); writer.writeByte((byte)':');\n");
			writeProperty(attr, true);
			code.append("\t\t\t\twriter.writeByte((byte)','); hasWritten = true;\n");
			code.append("\t\t\t}");
			if (attr.notNull && "null".equals(defaultValue)) {
				code.append(" else throw new com.dslplatform.json.SerializationException(\"Property '");
				code.append(attr.name).append("' is not allowed to be null\");\n");
			} else code.append("\n");
		}
		code.append("\t\t\treturn hasWritten;\n");
		code.append("\t\t}\n");
	}

	private void checkMandatory(final List<AttributeInfo> attributes, final int start) throws IOException {
		for (int i = start; i < attributes.size(); i++) {
			AttributeInfo attr = attributes.get(i);
			if (attr.mandatory) {
				code.append(" throw new java.io.IOException(\"Property '").append(attr.name);
				code.append("' is mandatory but was not found in JSON at position: \" + reader.positionInStream());\n");
				return;
			}
		}
		code.append(" return;\n");
	}

	private void checkMandatory(final List<AttributeInfo> attributes) throws IOException {
		for (AttributeInfo attr : attributes) {
			if (attr.mandatory) {
				code.append("\t\t\tif (!__detected_").append(attr.name).append("__) throw new java.io.IOException(\"Property '").append(attr.name);
				code.append("' is mandatory but was not found in JSON at position: \" + reader.positionInStream());\n");
			}
		}
	}

	void emptyCtorArray(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si,"Array_" + si.name, className, true);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si);
		writeArray(className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\t").append(className).append(" instance = new ").append(className).append("();\n");
		code.append("\t\t\tbind(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" bind(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.last() != '[') throw new java.io.IOException(\"Expecting '[' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\treader.getNextToken();\n");
			setPropertyValue(attr, "\t");
			i--;
			if (i > 0) code.append("\t\t\tif (reader.getNextToken() != ',') throw new java.io.IOException(\"Expecting ',' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		}
		code.append("\t\t\tif (reader.getNextToken() != ']') throw new java.io.IOException(\"Expecting ']' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	void fromCtorArray(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si,"Array_" + si.name, className, false);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si);
		writeArray(className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.wasNull()) return null;\n");
		code.append("\t\t\telse if (reader.last() != '[') throw new java.io.IOException(\"Expecting '[' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		code.append("\t\t\treturn readContent(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\tfinal ").append(attr.type.toString()).append(" _").append(attr.name).append("_;\n");
			code.append("\t\t\treader.getNextToken();\n");
			readPropertyValue(attr, "\t");
			i--;
			if (i > 0) code.append("\t\t\tif (reader.getNextToken() != ',') throw new java.io.IOException(\"Expecting ',' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		}
		code.append("\t\t\tif (reader.getNextToken() != ']') throw new java.io.IOException(\"Expecting ']' at position: \" + reader.positionInStream() + \". Found \" + (char) reader.last());\n");
		returnInstance("\t\t\t", si.constructor, className);
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	private void returnInstance(final String alignment, final ExecutableElement constructor, final String className) throws IOException {
		code.append(alignment).append("return new ").append(className).append("(");
		int i = constructor.getParameters().size();
		for (VariableElement p : constructor.getParameters()) {
			code.append("_").append(p.getSimpleName()).append("_");
			i--;
			if (i > 0) code.append(", ");
		}
		code.append(");\n");
	}

	private void writeArray(final String className, List<AttributeInfo> sortedAttributes) throws IOException {
		code.append("\t\tpublic final void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		code.append("\t\t\tif (instance == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		code.append("\t\t\t\twriter.writeByte((byte)'[');\n");
		code.append("\t\t\t\twriteContentFull(writer, instance);\n");
		code.append("\t\t\t\twriter.writeByte((byte)']');\n");
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic boolean writeContentMinimal(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		if (sortedAttributes.isEmpty()) {
			code.append("\t\t\treturn false;\n");
			code.append("\t\t}\n");
		} else {
			code.append("\t\t\twriteContentFull(writer, instance);\n");
			code.append("\t\t\treturn true;\n");
			code.append("\t\t}\n");
		}
		code.append("\t\tpublic void writeContentFull(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			writeProperty(attr, false);
			i--;
			if (i > 0) code.append("\t\t\twriter.writeByte((byte)',');\n");
		}
		code.append("\t\t}\n");
	}

	private void writeProperty(AttributeInfo attr, boolean checkedDefault) throws IOException {
		String typeName = attr.type.toString();
		String readValue = "instance." + attr.readProperty;
		OptimizedConverter converter = context.inlinedConverters.get(typeName);
		boolean canBeNull = !checkedDefault && (converter == null || converter.defaultValue == null);
		if (attr.notNull && canBeNull) {
			code.append("\t\t\tif (").append(readValue);
			code.append(" == null) throw new com.dslplatform.json.SerializationException(\"Property '").append(attr.name).append("' is not allowed to be null\");\n");
			code.append("\t\t\t");
		} else if (canBeNull) {
			code.append("\t\t\tif (").append(readValue).append(" == null) writer.writeNull();\n");
			code.append("\t\t\telse ");
		} else {
			code.append("\t\t\t");
		}
		if (checkedDefault) code.append("\t");
		if (attr.converter != null) {
			code.append(attr.converter.toString()).append(".JSON_WRITER.write(writer, ").append(readValue).append(")");
		} else {
			if (converter != null) {
				code.append(converter.nonNullableEncoder("writer", readValue));
			} else if (attr.isEnum()) {
				code.append("Enum_").append(attr.target.name).append(".writeStatic(writer, ").append(readValue).append(")");
			} else {
				code.append("writer_").append(attr.name).append("().write(writer, ").append(readValue).append(")");
			}
		}
		code.append(";\n");
	}

	private void handleSwitch(StructInfo si, String alignment, boolean localNames) throws IOException {
		for (AttributeInfo attr : si.attributes.values()) {
			String mn = si.minifiedNames.get(attr.id);
			code.append(alignment).append("\tcase ").append(Integer.toString(StructInfo.calcHash(mn != null ? mn : attr.id))).append(":\n");
			for(String an : attr.alternativeNames) {
				code.append(alignment).append("\tcase ").append(Integer.toString(StructInfo.calcHash(an))).append(":\n");
			}
			if (attr.fullMatch) {
				code.append(alignment).append("\t\tif (!reader.wasLastName(name_").append(attr.name).append(")) {\n");
				if (si.onUnknown == CompiledJson.Behavior.FAIL) {
					code.append(alignment).append("\t\tthrow new java.io.IOException(\"Unknown property detected: '\" + reader.getLastName()");
					code.append(" + \"' at position: \" + reader.positionInStream(reader.getLastName().length() + 3));\n");
				} else {
					code.append(alignment).append("\t\treader.getNextToken(); reader.skip(); break;\n");
				}
				code.append(alignment).append("\t\t}\n");
			}
			if (attr.mandatory) {
				code.append(alignment).append("\t\t__detected_").append(attr.name).append("__ = true;\n");
			}
			code.append(alignment).append("\t\treader.getNextToken();\n");
			if (localNames) readPropertyValue(attr, alignment);
			else setPropertyValue(attr, alignment);
			code.append(alignment).append("\t\treader.getNextToken();\n");
			code.append(alignment).append("\t\tbreak;\n");
		}
		code.append(alignment).append("\tdefault:\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) {
			code.append(alignment).append("\t\tString lastName = reader.getLastName();\n");
			code.append(alignment).append("\t\tthrow new java.io.IOException(\"Unknown property detected: '\" + lastName");
			code.append(" + \"' at position: \" + reader.positionInStream(lastName.length() + 3));\n");
		} else {
			code.append(alignment).append("\t\treader.getNextToken();\n");
			code.append(alignment).append("\t\treader.skip();\n");
		}
	}

	private void setPropertyValue(AttributeInfo attr, String alignment) throws IOException {
		if (attr.notNull) {
			code.append(alignment).append("\t\tif (reader.wasNull()) throw new java.io.IOException(\"Property '").append(attr.name).append("' is not allowed to be null.");
			code.append(" Null value found at position: \" + reader.positionInStream());\n");
		}
		String typeName = attr.type.toString();
		OptimizedConverter converter = context.inlinedConverters.get(typeName);
		if (attr.converter == null && converter != null && converter.defaultValue == null && !attr.notNull && converter.hasNonNullableMethod()) {
			code.append(alignment).append("\t\tif (reader.wasNull()) instance.");
			if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = null;\n");
			else code.append(attr.writeMethod.getSimpleName()).append("(null);\n");
			code.append(alignment).append("\t\telse instance.");
			if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
			else code.append(attr.writeMethod.getSimpleName()).append("(");
			code.append(converter.nonNullableDecoder()).append("(reader)");
		} else {
			code.append(alignment).append("\t\tinstance.");
			if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
			else code.append(attr.writeMethod.getSimpleName()).append("(");
			if (attr.converter != null || converter != null) {
				if (attr.converter != null) code.append(attr.converter.toString()).append(".JSON_READER.read(reader)");
				else code.append(converter.nonNullableDecoder()).append("(reader)");
			} else if (attr.isEnum()) {
				if (!attr.notNull) code.append("reader.wasNull() ? null : ");
				code.append("Enum_").append(attr.target.name).append(".readStatic(reader)");
			} else {
				code.append("reader_").append(attr.name).append("().read(reader)");
			}
		}
		if (attr.field == null) code.append(")");
		code.append(";\n");
	}

	private void readPropertyValue(AttributeInfo attr, String alignment) throws IOException {
		if (attr.notNull) {
			code.append(alignment).append("\t\tif (reader.wasNull()) throw new java.io.IOException(\"Property '").append(attr.name).append("' is not allowed to be null.");
			code.append(" Null value found at position: \" + reader.positionInStream());\n");
		}
		String typeName = attr.type.toString();
		OptimizedConverter converter = context.inlinedConverters.get(typeName);
		if (attr.converter == null && converter != null && converter.defaultValue == null && !attr.notNull && converter.hasNonNullableMethod()) {
			code.append(alignment).append("\t\t_").append(attr.name).append("_ = reader.wasNull() ? null : ");
			code.append(converter.nonNullableDecoder());
		} else {
			code.append(alignment).append("\t\t_").append(attr.name).append("_ = ");
			if (attr.converter != null || converter != null) {
				if (attr.converter != null) code.append(attr.converter.toString()).append(".JSON_READER.read");
				else code.append(converter.nonNullableDecoder());
			} else if (attr.isEnum()) {
				if (!attr.notNull) code.append("reader.wasNull() ? null : ");
				code.append("Enum_").append(attr.target.name).append(".readStatic");
			} else {
				code.append("reader_").append(attr.name).append("().read");
			}
		}
		code.append("(reader);\n");
	}

	private static int calcWeakHash(String name) {
		int hash = 0;
		for (int i = 0; i < name.length(); i++) {
			hash += (byte) name.charAt(i);
		}
		return hash;
	}
}
