package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;

@SupportedAnnotationTypes({"com.dslplatform.json.CompiledJson", "com.dslplatform.json.JsonAttribute", "com.dslplatform.json.JsonConverter"})
@SupportedOptions({"dsljson.loglevel", "dsljson.annotation"})
public class CompiledJsonAnnotationProcessor extends AbstractProcessor {

	private static final Set<String> JsonIgnore;
	private static final Set<String> NonNullable;
	private static final Set<String> PropertyAlias;
	private static final Map<String, String> JsonRequired;

	private static final String CONFIG = "META-INF/services/com.dslplatform.json.Configuration";

	static {
		JsonIgnore = new HashSet<>();
		JsonIgnore.add("com.fasterxml.jackson.annotation.JsonIgnore");
		NonNullable = new HashSet<>();
		NonNullable.add("javax.validation.constraints.NotNull");
		NonNullable.add("javax.annotation.Nonnull");
		NonNullable.add("android.support.annotation.NonNull");
		PropertyAlias = new HashSet<>();
		PropertyAlias.add("com.fasterxml.jackson.annotation.JsonProperty");
		PropertyAlias.add("com.google.gson.annotations.SerializedName");
		JsonRequired = new HashMap<>();
		JsonRequired.put("com.fasterxml.jackson.annotation.JsonProperty", "required()");
	}

	private LogLevel logLevel = LogLevel.ERRORS;
	private AnnotationUsage annotationUsage = AnnotationUsage.IMPLICIT;

	private Analysis analysis;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Map<String, String> options = processingEnv.getOptions();
		String ll = options.get("dsljson.loglevel");
		if (ll != null && ll.length() > 0) {
			logLevel = LogLevel.valueOf(ll);
		}
		String au = options.get("dsljson.annotation");
		if (au != null && au.length() > 0) {
			annotationUsage = AnnotationUsage.valueOf(au);
		}
		DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
		Set<Type> knownEncoders = dslJson.getRegisteredEncoders();
		Set<Type> knownDecoders = dslJson.getRegisteredDecoders();
		Set<String> allTypes = new HashSet<>();
		for (Type t : knownEncoders) {
			if (knownDecoders.contains(t)) {
				allTypes.add(t.getTypeName());
			}
		}
		analysis = new Analysis(
				processingEnv,
				annotationUsage,
				logLevel,
				allTypes,
				JsonIgnore,
				NonNullable,
				PropertyAlias,
				JsonRequired,
				true,
				true,
				true);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.compiledJsonElement);
		if (!compiledJsons.isEmpty()) {
			Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(analysis.converterElement);
			List<String> configurations = analysis.processConverters(jsonConverters);
			Map<String, StructInfo> structs = analysis.processCompiledJson(compiledJsons);
			if (analysis.hasError()) {
				return false;
			}
			String code = buildCode(structs);

			try {
				String className = "dsl_json_Annotation_Processor_External_Serialization";
				Writer writer = processingEnv.getFiler().createSourceFile(className).openWriter();
				writer.write(code);
				writer.close();
				writer = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", CONFIG).openWriter();
				writer.write(className);
				for (String conf : configurations) {
					writer.write('\n');
					writer.write(conf);
				}
				writer.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization files");
			}
		}
		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		SourceVersion latest = SourceVersion.latest();
		if ("RELEASE_9".equals(latest.name())) {
			return latest;
		}
		return SourceVersion.RELEASE_8;
	}

	private static String nonGenericObject(String type) {
		String objectType = Analysis.objectName(type);
		int genInd = objectType.indexOf('<');
		if (genInd == -1) return objectType;
		return objectType.substring(0, genInd);
	}

	private static String typeOrClass(String objectType, String typeName) {
		if (objectType.equals(typeName)) return objectType + ".class";
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return typeName + ".class";
		return "new com.dslplatform.json.runtime.TypeDefinition<" + typeName + ">(){}.type";
	}

	private String buildCode(Map<String, StructInfo> structs) {
		final StringBuilder code = new StringBuilder();
		code.append("public class dsl_json_Annotation_Processor_External_Serialization implements com.dslplatform.json.Configuration {\n");
		code.append("\t@Override\n");
		code.append("\tpublic void configure(com.dslplatform.json.DslJson json) {\n");
		for (StructInfo si : structs.values()) {
			if (si.type == ObjectType.CLASS && si.hasEmptyCtor && !si.attributes.isEmpty()) {
				code.append("\t\tregister_").append(si.name).append("(json);\n");
			}
		}
		code.append("\t}\n");
		for (Map.Entry<String, StructInfo> it : structs.entrySet()) {
			StructInfo si = it.getValue();
			String className = it.getKey();
			if (si.type == ObjectType.CLASS && si.hasEmptyCtor && !si.attributes.isEmpty()) {
				code.append("\tprivate static void register_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
				code.append("\t\tcom.dslplatform.json.runtime.BeanDescription<").append(className).append("> description = new com.dslplatform.json.runtime.BeanDescription<>(\n");
				code.append("\t\t\t").append(className).append(".class,\n");
				code.append("\t\t\t").append(className).append("::new,\n");
				code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
				for (AttributeInfo attr : si.attributes.values()) {
					addAttributeWriter(code, className, attr);
				}
				code.setLength(code.length() - 2);
				code.append("\n\t\t\t},\n");
				code.append("\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo[] {\n");
				for (AttributeInfo attr : si.attributes.values()) {
					String mn = si.minifiedNames.get(attr.id);
					addAttributeReader(code, className, attr, mn != null ? mn : attr.id);
					for (String an : attr.alternativeNames) {
						addAttributeReader(code, className, attr, an);
					}
				}
				code.setLength(code.length() - 2);
				code.append("\n\t\t\t},\n");
				code.append("\t\t\t");
				if (si.onUnknown == CompiledJson.Behavior.FAIL) code.append("false");
				else code.append("true");
				code.append(");\n");
				code.append("\t\tjson.registerReader(").append(className).append(".class, description);\n");
				code.append("\t\tjson.registerWriter(").append(className).append(".class, description);\n");
				code.append("\t\tjson.registerBinder(").append(className).append(".class, description);\n");
				code.append("\t}\n");
			}
		}
		code.append("}\n");
		return code.toString();
	}

	private static void addAttributeWriter(StringBuilder code, String className, AttributeInfo attr) {
		if (attr.converter != null) {
			code.append("\t\t\t\tnew com.dslplatform.json.runtime.AttributeEncoder<>(");
			if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
			else code.append("c -> c.").append(attr.field.getSimpleName());
			code.append(", \"").append(attr.id).append("\", !json.omitDefaults, ");
			code.append(attr.converter.toString()).append(".JSON_WRITER),\n");
		} else {
			String objectType = nonGenericObject(attr.type.toString());
			code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
			code.append(className).append(", ").append(objectType).append(">createEncoder(");
			if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
			else code.append("c -> c.").append(attr.field.getSimpleName());
			code.append(", \"").append(attr.id).append("\", json, ");
			code.append(typeOrClass(objectType, attr.type.toString())).append("),\n");
		}
	}

	private static void addAttributeReader(StringBuilder code, String className, AttributeInfo attr, String alias) {
		if (attr.converter != null) {
			code.append("\t\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo<>(\"");
			code.append(alias).append("\", ");
			if (attr.fullMatch) code.append("true, ");
			else code.append("false, ");
			if (attr.mandatory) code.append("true, ");
			else code.append("false, ");
			code.append("new com.dslplatform.json.runtime.AttributeDecoder<>((i, v) -> i.");
			if (attr.writeMethod != null) {
				code.append(attr.writeMethod.getSimpleName()).append("(").append("v").append(")");
			} else {
				code.append(attr.field.getSimpleName()).append(" = ").append("v");
			}
			code.append(", ").append(attr.converter.toString()).append(".JSON_READER)),\n");
		} else {
			String objectType = nonGenericObject(attr.type.toString());
			code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
			code.append(className).append(", ").append(objectType).append(">createDecoder((i, v) -> i.");
			if (attr.writeMethod != null) {
				code.append(attr.writeMethod.getSimpleName()).append("(").append("v").append(")");
			} else {
				code.append(attr.field.getSimpleName()).append(" = ").append("v");
			}
			code.append(", \"").append(alias).append("\", json, ");
			if (attr.fullMatch) code.append("true, ");
			else code.append("false, ");
			if (attr.mandatory) code.append("true, ");
			else code.append("false, ");
			code.append(typeOrClass(objectType, attr.type.toString())).append("),\n");
		}
	}
}
