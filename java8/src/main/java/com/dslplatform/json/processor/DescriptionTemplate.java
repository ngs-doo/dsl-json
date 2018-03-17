package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

class DescriptionTemplate {

	private final Writer code;
	private final Context context;

	public DescriptionTemplate(Context context) {
		this.code = context.code;
		this.context = context;
	}

	void emptyCtorObject(final StructInfo si, final String className) throws IOException {
		code.append("\tprivate static com.dslplatform.json.runtime.ObjectFormatDescription<").append(className).append(", ");
		code.append(className).append("> register_object_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn com.dslplatform.json.runtime.ObjectFormatDescription.create(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(className).append("::new,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		final List<AttributeInfo> sortedAttributes = Context.sortedAttributes(si);
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			addAttributeWriter(className, attr);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo[] {\n");
		i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			String mn = si.minifiedNames.get(attr.id);
			final String readValue = attr.writeMethod != null
					? className + "::" + attr.writeMethod.getSimpleName()
					: "(i, v) -> i." + attr.field.getSimpleName() + " = v";
			addAttributeReader(className, attr, mn != null ? mn : attr.id, readValue);
			for (String an : attr.alternativeNames) {
				code.append(",\n");
				addAttributeReader(className, attr, an, readValue);
			}
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tjson,\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) code.append("\t\t\tfalse\n");
		else code.append("\t\t\ttrue\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	void emptyCtorArray(final StructInfo si, final String className) throws IOException {
		code.append("\tprivate static com.dslplatform.json.runtime.ArrayFormatDescription<").append(className).append(", ");
		code.append(className).append("> register_array_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn com.dslplatform.json.runtime.ArrayFormatDescription.create(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(className).append("::new,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		final List<AttributeInfo> sortedAttributes = Context.sortedAttributes(si);
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			addArrayWriter(className, attr);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonReader.BindObject[] {\n");
		i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			final String readValue = attr.writeMethod != null
					? className + "::" + attr.writeMethod.getSimpleName()
					: "(i, v) -> i." + attr.field.getSimpleName() + " = v";
			addArrayReader(className, attr, readValue);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	private void addAttributeWriter(final String className, final AttributeInfo attr) throws IOException {
		code.append("\t\t\t\t");
		context.addAttributeWriter(className, attr);
	}

	private void addArrayWriter(final String className, final AttributeInfo attr) throws IOException {
		code.append("\t\t\t\t");
		context.addArrayWriter(className, attr);
	}

	private void addAttributeReader(final String className, final AttributeInfo attr, final String alias, final String readValue) throws IOException {
		code.append("\t\t\t\t");
		context.addAttributeReader(className, attr, alias, readValue);
	}

	private void addArrayReader(final String className, final AttributeInfo attr, final String readValue) throws IOException {
		code.append("\t\t\t\t");
		context.addArrayReader(className, attr, readValue);
	}

	void fromCtorObject(final StructInfo si, final String className) throws IOException {
		final String builderName = "Builder" + si.name;
		code.append("\tprivate static com.dslplatform.json.runtime.ObjectFormatDescription<").append(builderName);
		code.append(", ").append(className).append("> register_object_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn new com.dslplatform.json.runtime.ObjectFormatDescription<");
		code.append(builderName).append(", ").append(className).append(">(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(builderName).append("::new,\n");
		code.append("\t\t\t").append(builderName).append("::__buildFromBuilder__,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		final List<AttributeInfo> sortedAttributes = Context.sortedAttributes(si);
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			addAttributeWriter(className, attr);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo[] {\n");
		i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			String mn = si.minifiedNames.get(attr.id);
			final String readValue = builderName + "::_" + attr.name + "_";
			addAttributeReader(builderName, attr, mn != null ? mn : attr.id, readValue);
			for (String an : attr.alternativeNames) {
				code.append(",\n");
				addAttributeReader(builderName, attr, an, readValue);
			}
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tjson,\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) code.append("\t\t\tfalse\n");
		else code.append("\t\t\ttrue\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	void fromCtorArray(final StructInfo si, final String className) throws IOException {
		final String builderName = "Builder" + si.name;
		code.append("\tprivate static com.dslplatform.json.runtime.ArrayFormatDescription<").append(builderName);
		code.append(", ").append(className).append("> register_array_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn new com.dslplatform.json.runtime.ArrayFormatDescription<");
		code.append(builderName).append(", ").append(className).append(">(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(builderName).append("::new,\n");
		code.append("\t\t\t").append(builderName).append("::__buildFromBuilder__,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		final List<AttributeInfo> sortedAttributes = Context.sortedAttributes(si);
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			addArrayWriter(className, attr);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonReader.BindObject[] {\n");
		i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			final String readValue = builderName + "::_" + attr.name + "_";
			addArrayReader(builderName, attr, readValue);
			i--;
			if (i > 0) code.append(",\n");
		}
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}
}
