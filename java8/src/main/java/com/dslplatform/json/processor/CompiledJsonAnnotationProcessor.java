package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;

@SupportedAnnotationTypes({"com.dslplatform.json.CompiledJson", "com.dslplatform.json.JsonAttribute", "com.dslplatform.json.JsonConverter", "com.fasterxml.jackson.annotation.JsonCreator"})
@SupportedOptions({"dsljson.loglevel", "dsljson.annotation", "dsljson.unknown", "dsljson.inline", "dsljson.jackson"})
public class CompiledJsonAnnotationProcessor extends AbstractProcessor {

	private static final Set<String> JsonIgnore;
	private static final Set<String> NonNullable;
	private static final Set<String> PropertyAlias;
	private static final Map<String, String> JsonRequired;
	private static final Set<String> Constructors;
	private static final Map<String, String> InlinedEncoders;
	private static final Map<String, String> InlinedDecoders;

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
		Constructors = new HashSet<>();
		Constructors.add("com.fasterxml.jackson.annotation.JsonCreator");
		InlinedEncoders = new HashMap<>();
		InlinedDecoders = new HashMap<>();
		InlinedEncoders.put("int", "com.dslplatform.json.runtime.Converters::encodeInt");
		InlinedDecoders.put("int", "com.dslplatform.json.runtime.Converters::decodeInt");
		InlinedEncoders.put("java.lang.Integer", "com.dslplatform.json.runtime.Converters::encodeIntNullable");
		InlinedDecoders.put("java.lang.Integer", "com.dslplatform.json.runtime.Converters::decodeIntNullable");
		InlinedEncoders.put("long", "com.dslplatform.json.runtime.Converters::encodeLong");
		InlinedDecoders.put("long", "com.dslplatform.json.runtime.Converters::decodeLong");
		InlinedEncoders.put("java.lang.Long", "com.dslplatform.json.runtime.Converters::encodeLongNullable");
		InlinedDecoders.put("java.lang.Long", "com.dslplatform.json.runtime.Converters::decodeLongNullable");
		InlinedEncoders.put("float", "com.dslplatform.json.runtime.Converters::encodeFloat");
		InlinedDecoders.put("float", "com.dslplatform.json.runtime.Converters::decodeFloat");
		InlinedEncoders.put("java.lang.Float", "com.dslplatform.json.runtime.Converters::encodeFloatNullable");
		InlinedDecoders.put("java.lang.Float", "com.dslplatform.json.runtime.Converters::decodeFloatNullable");
		InlinedEncoders.put("double", "com.dslplatform.json.runtime.Converters::encodeDouble");
		InlinedDecoders.put("double", "com.dslplatform.json.runtime.Converters::decodeDouble");
		InlinedEncoders.put("java.lang.Double", "com.dslplatform.json.runtime.Converters::encodeDoubleNullable");
		InlinedDecoders.put("java.lang.Double", "com.dslplatform.json.runtime.Converters::decodeDoubleNullable");
		InlinedEncoders.put("java.lang.String", "com.dslplatform.json.runtime.Converters::encodeStringNullable");
		InlinedDecoders.put("java.lang.String", "com.dslplatform.json.runtime.Converters::decodeStringNullable");
	}

	private LogLevel logLevel = LogLevel.ERRORS;
	private AnnotationUsage annotationUsage = AnnotationUsage.IMPLICIT;
	private UnknownTypes unknownTypes = UnknownTypes.ERROR;
	private boolean allowInline = true;
	private boolean withJackson = true;

	private Analysis analysis;
	private TypeElement jacksonCreatorElement;
	private DeclaredType jacksonCreatorType;

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
		String unk = options.get("dsljson.unknown");
		if (unk != null && unk.length() > 0) {
			unknownTypes = UnknownTypes.valueOf(unk);
		}
		String inl = options.get("dsljson.inline");
		if (inl != null && inl.length() > 0) {
			allowInline = Boolean.parseBoolean(inl);
		}
		String jks = options.get("dsljson.jackson");
		if (jks != null && jks.length() > 0) {
			withJackson = Boolean.parseBoolean(jks);
		}
		final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
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
				rawClass -> {
					try {
						Class<?> raw = Class.forName(rawClass);
						return dslJson.canSerialize(raw) && dslJson.canDeserialize(raw);
					} catch (Exception ignore) {
						return false;
					}
				},
				JsonIgnore,
				NonNullable,
				PropertyAlias,
				JsonRequired,
				Constructors,
				unknownTypes,
				false,
				true,
				true,
				true);
		jacksonCreatorElement = processingEnv.getElementUtils().getTypeElement("com.fasterxml.jackson.annotation.JsonCreator");
		jacksonCreatorType = jacksonCreatorElement != null ? processingEnv.getTypeUtils().getDeclaredType(jacksonCreatorElement) : null;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.compiledJsonElement);
		Set<? extends Element> jacksonCreators = withJackson && jacksonCreatorElement != null ? roundEnv.getElementsAnnotatedWith(jacksonCreatorElement) : new HashSet<>();
		if (!compiledJsons.isEmpty() || !jacksonCreators.isEmpty()) {
			Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(analysis.converterElement);
			List<String> configurations = analysis.processConverters(jsonConverters);
			analysis.processAnnotation(analysis.compiledJsonType, compiledJsons);
			if (!jacksonCreators.isEmpty() && jacksonCreatorType != null) {
				analysis.processAnnotation(jacksonCreatorType, jacksonCreators);
			}
			Map<String, StructInfo> structs = analysis.analyze();
			if (analysis.hasError()) {
				return false;
			}
			String code = buildCode(structs, allowInline);

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

	private static String buildCode(final Map<String, StructInfo> structs, final boolean allowInline) {
		final StringBuilder code = new StringBuilder();
		code.append("public class dsl_json_Annotation_Processor_External_Serialization implements com.dslplatform.json.Configuration {\n");
		code.append("\t@Override\n");
		code.append("\tpublic void configure(com.dslplatform.json.DslJson json) {\n");
		for (Map.Entry<String, StructInfo> kv : structs.entrySet()) {
			final String className = kv.getKey();
			final StructInfo si = kv.getValue();
			if (si.type == ObjectType.CLASS && si.constructor != null && !si.attributes.isEmpty()) {
				String descriptionName = si.name;
				if (si.formats.contains(CompiledJson.Format.OBJECT)) {
					code.append("\t\tcom.dslplatform.json.runtime.ObjectFormatDescription object_").append(si.name);
					code.append(" = register_object_").append(si.name).append("(json);\n");
					descriptionName = "object_" + si.name;
				}
				if (si.formats.contains(CompiledJson.Format.ARRAY)) {
					code.append("\t\tcom.dslplatform.json.runtime.ArrayFormatDescription array_").append(si.name);
					code.append(" = register_array_").append(si.name).append("(json);\n");
					descriptionName = "array_" + si.name;
				}
				if (si.formats.contains(CompiledJson.Format.OBJECT) && si.formats.contains(CompiledJson.Format.ARRAY)) {
					descriptionName = si.name;
					code.append("\t\tcom.dslplatform.json.runtime.ObjectDescription ").append(descriptionName).append(" = new com.dslplatform.json.runtime.ObjectDescription(\n");
					code.append("\t\t\t").append(className).append(".class,\n");
					code.append("\t\t\tobject_").append(si.name).append(",\n");
					code.append("\t\t\tarray_").append(si.name).append(",\n");
					if (si.isObjectFormatFirst) code.append("\t\t\ttrue,\n");
					else code.append("\t\t\tfalse,\n");
					String typeAlias = si.deserializeName.isEmpty() ? className : si.deserializeName;
					code.append("\t\t\t\"").append(typeAlias).append("\",\n");
					code.append("\t\t\tjson);\n");
					if (si.hasEmptyCtor()) {
						code.append("\t\tjson.registerBinder(").append(className).append(".class, ").append(si.name).append(");\n");
					}
					code.append("\t\tjson.registerReader(").append(className).append(".class, ").append(si.name).append(");\n");
					code.append("\t\tjson.registerWriter(").append(className).append(".class, ").append(si.name).append(");\n");
				} else {
					if (si.hasEmptyCtor()) {
						code.append("\t\tjson.registerBinder(").append(className).append(".class, ").append(descriptionName).append(");\n");
					}
					code.append("\t\tjson.registerReader(").append(className).append(".class, ").append(descriptionName).append(");\n");
					code.append("\t\tjson.registerWriter(").append(className).append(".class, ").append(descriptionName).append(");\n");
				}
			} else if (si.type == ObjectType.CONVERTER) {
				String type = typeOrClass(nonGenericObject(className), className);
				code.append("\t\tjson.registerWriter(").append(type).append(", ").append(si.converter).append(".JSON_WRITER);\n");
				code.append("\t\tjson.registerReader(").append(type).append(", ").append(si.converter).append(".JSON_READER);\n");
			}
		}
		for (Map.Entry<String, StructInfo> kv : structs.entrySet()) {
			StructInfo si = kv.getValue();
			if (si.type == ObjectType.MIXIN && !si.implementations.isEmpty() && si.deserializeAs == null) {
				code.append("\t\tregister_").append(si.name).append("(json");
				for (StructInfo im : si.implementations) {
					if (im.formats.contains(CompiledJson.Format.OBJECT) && im.formats.contains(CompiledJson.Format.ARRAY)) {
						code.append(", ").append(im.name);
					} else if (im.formats.contains(CompiledJson.Format.OBJECT)) {
						code.append(", object_").append(im.name);
					} else if (im.formats.contains(CompiledJson.Format.ARRAY)) {
						code.append(", array_").append(im.name);
					}
				}
				code.append(");\n");
			} else if (si.type == ObjectType.MIXIN && si.deserializeAs != null) {
				String typeMixin = typeOrClass(nonGenericObject(kv.getKey()), kv.getKey());
				StructInfo target = si.deserializeTarget();
				code.append("\t\tjson.registerReader(").append(typeMixin).append(", ");
				if (!target.formats.contains(CompiledJson.Format.OBJECT)) {
					code.append("array_");
				} else if (!target.formats.contains(CompiledJson.Format.ARRAY)) {
					code.append("object_");
				}
				code.append(target.name).append(");\n");
			}
		}
		code.append("\t}\n");
		for (Map.Entry<String, StructInfo> it : structs.entrySet()) {
			StructInfo si = it.getValue();
			String className = it.getKey();
			if (si.type == ObjectType.CLASS && !si.attributes.isEmpty()) {
				if (si.hasEmptyCtor()) {
					if (si.formats.contains(CompiledJson.Format.OBJECT)) {
						emptyCtorObjectDescription(code, si, className, structs, allowInline);
					}
					if (si.formats.contains(CompiledJson.Format.ARRAY)) {
						emptyCtorArrayDescription(code, si, className, structs, allowInline);
					}
				} else if (si.constructor != null) {
					code.append("\tprivate static class Builder").append(si.name).append(" {\n");
					for (VariableElement p : si.constructor.getParameters()) {
						//TODO: default
						code.append("\t\tprivate ").append(p.asType()).append(" _").append(p.getSimpleName()).append("_;\n");
						code.append("\t\tvoid _").append(p.getSimpleName()).append("_(").append(p.asType()).append(" v) { this._").append(p.getSimpleName()).append("_ = v; }\n");
					}
					code.append("\t\tpublic ").append(className).append(" __buildFromBuilder__() {\n");
					code.append("\t\t\treturn new ").append(className).append("(");
					for (VariableElement p : si.constructor.getParameters()) {
						code.append("_").append(p.getSimpleName()).append("_, ");
					}
					code.setLength(code.length() - 2);
					code.append(");\n");
					code.append("\t\t}\n");
					code.append("\t}\n");
					if (si.formats.contains(CompiledJson.Format.OBJECT)) {
						fromCtorObjectDescription(code, si, className, structs, allowInline);
					}
					if (si.formats.contains(CompiledJson.Format.ARRAY)) {
						fromCtorArrayDescription(code, si, className, structs, allowInline);
					}
				}
			} else if (si.type == ObjectType.MIXIN && !si.implementations.isEmpty() && si.deserializeAs == null) {
				mixinDescription(code, si, className, structs);
			}
		}
		code.append("}\n");
		return code.toString();
	}

	private static void fromCtorObjectDescription(final StringBuilder code, final StructInfo si, final String className, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String builderName = "Builder" + si.name;
		code.append("\tprivate static com.dslplatform.json.runtime.ObjectFormatDescription<").append(builderName);
		code.append(", ").append(className).append("> register_object_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn new com.dslplatform.json.runtime.ObjectFormatDescription<");
		code.append(builderName).append(", ").append(className).append(">(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(builderName).append("::new,\n");
		code.append("\t\t\t").append(builderName).append("::__buildFromBuilder__,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			addAttributeWriter(code, className, attr, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			String mn = si.minifiedNames.get(attr.id);
			final String readValue = builderName + "::_" + attr.name + "_";
			addAttributeReader(code, builderName, attr, mn != null ? mn : attr.id, readValue, structs, allowInline);
			for (String an : attr.alternativeNames) {
				addAttributeReader(code, builderName, attr, an, readValue, structs, allowInline);
			}
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tjson,\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) code.append("\t\t\tfalse\n");
		else code.append("\t\t\ttrue\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	private static void fromCtorArrayDescription(final StringBuilder code, final StructInfo si, final String className, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String builderName = "Builder" + si.name;
		code.append("\tprivate static com.dslplatform.json.runtime.ArrayFormatDescription<").append(builderName);
		code.append(", ").append(className).append("> register_array_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn new com.dslplatform.json.runtime.ArrayFormatDescription<");
		code.append(builderName).append(", ").append(className).append(">(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(builderName).append("::new,\n");
		code.append("\t\t\t").append(builderName).append("::__buildFromBuilder__,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			addArrayWriter(code, className, attr, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonReader.BindObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			final String readValue = builderName + "::_" + attr.name + "_";
			addArrayReader(code, builderName, attr, readValue, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	private static void emptyCtorObjectDescription(final StringBuilder code, final StructInfo si, final String className, final Map<String, StructInfo> structs, final boolean allowInline) {
		code.append("\tprivate static com.dslplatform.json.runtime.ObjectFormatDescription<").append(className).append(", ");
		code.append(className).append("> register_object_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn com.dslplatform.json.runtime.ObjectFormatDescription.create(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(className).append("::new,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			addAttributeWriter(code, className, attr, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.runtime.DecodePropertyInfo[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			String mn = si.minifiedNames.get(attr.id);
			final String readValue = attr.writeMethod != null
					? className + "::" + attr.writeMethod.getSimpleName()
					: "(i, v) -> i." + attr.field.getSimpleName() + " = v";
			addAttributeReader(code, className, attr, mn != null ? mn : attr.id, readValue, structs, allowInline);
			for (String an : attr.alternativeNames) {
				addAttributeReader(code, className, attr, an, readValue, structs, allowInline);
			}
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tjson,\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) code.append("\t\t\tfalse\n");
		else code.append("\t\t\ttrue\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	private static void emptyCtorArrayDescription(final StringBuilder code, final StructInfo si, final String className, final Map<String, StructInfo> structs, final boolean allowInline) {
		code.append("\tprivate static com.dslplatform.json.runtime.ArrayFormatDescription<").append(className).append(", ");
		code.append(className).append("> register_array_").append(si.name).append("(com.dslplatform.json.DslJson json) {\n");
		code.append("\t\treturn com.dslplatform.json.runtime.ArrayFormatDescription.create(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\t").append(className).append("::new,\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonWriter.WriteObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			addArrayWriter(code, className, attr, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t},\n");
		code.append("\t\t\tnew com.dslplatform.json.JsonReader.BindObject[] {\n");
		for (AttributeInfo attr : si.attributes.values()) {
			final String readValue = attr.writeMethod != null
					? className + "::" + attr.writeMethod.getSimpleName()
					: "(i, v) -> i." + attr.field.getSimpleName() + " = v";
			addArrayReader(code, className, attr, readValue, structs, allowInline);
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		code.append("\t}\n");
	}

	private static void mixinDescription(final StringBuilder code, final StructInfo si, final String className, final Map<String, StructInfo> structs) {
		code.append("\tprivate static com.dslplatform.json.runtime.MixinDescription<").append(className).append("> register_").append(si.name).append("(com.dslplatform.json.DslJson json");
		for(StructInfo im : si.implementations) {
			if (im.formats.contains(CompiledJson.Format.OBJECT) && im.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append(", com.dslplatform.json.runtime.ObjectDescription ").append(im.name);
			} else if (im.formats.contains(CompiledJson.Format.OBJECT)) {
				code.append(", com.dslplatform.json.runtime.ObjectFormatDescription object_").append(im.name);
			} else if (im.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append(", com.dslplatform.json.runtime.ArrayFormatDescription array_").append(im.name);
			}
		}
		code.append(") {\n");
		code.append("\t\tcom.dslplatform.json.runtime.MixinDescription<").append(className).append("> description = new com.dslplatform.json.runtime.MixinDescription<>(\n");
		code.append("\t\t\t").append(className).append(".class,\n");
		code.append("\t\t\tjson,\n");
		code.append("\t\t\tnew com.dslplatform.json.runtime.ObjectDescription[] {\n");
		for (StructInfo im : si.implementations) {
			if (im.formats.contains(CompiledJson.Format.OBJECT) && im.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("\t\t\t").append(im.name).append(",\n");
			} else {
				code.append("\t\t\t\tnew com.dslplatform.json.runtime.ObjectDescription(");
				code.append(im.element.getQualifiedName()).append(".class, ");
				if (im.formats.contains(CompiledJson.Format.OBJECT)) {
					code.append("object_").append(im.name).append(", ");
				} else {
					code.append("null, ");
				}
				if (im.formats.contains(CompiledJson.Format.ARRAY)) {
					code.append("array_").append(im.name).append(", ");
				} else {
					code.append("null, ");
				}
				if (im.isObjectFormatFirst) code.append("true, ");
				else code.append("false, ");
				String typeAlias = im.deserializeName.isEmpty()
						? im.element.getQualifiedName().toString()
						: im.deserializeName;
				code.append("\"").append(typeAlias).append("\", json),\n");
			}
		}
		code.setLength(code.length() - 2);
		code.append("\n\t\t\t}\n");
		code.append("\t\t);\n");
		code.append("\t\tjson.registerReader(").append(className).append(".class, description);\n");
		code.append("\t\tjson.registerWriter(").append(className).append(".class, description);\n");
		code.append("\t\treturn description;\n");
		code.append("\t}\n");
	}

	private static String findConverter(AttributeInfo attr, Map<String, StructInfo> structs) {
		if (attr.converter != null) return attr.converter.toString();
		StructInfo target = structs.get(attr.type.toString());
		if (target == null || target.type != ObjectType.CONVERTER) return null;
		return target.converter;
	}

	private static void addAttributeWriter(final StringBuilder code, final String className, final AttributeInfo attr, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		final String converter = findConverter(attr, structs);
		String inline = allowInline ? InlinedEncoders.get(actualType) : null;
		code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createEncoder(");
		if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
		else code.append("c -> c.").append(attr.field.getSimpleName());
		code.append(", \"").append(attr.id).append("\", json, ");
		if (converter != null) code.append(converter).append(".JSON_WRITER),\n");
		else if (inline != null) code.append(inline).append("),\n");
		else code.append(typeOrClass(objectType, attr.type.toString())).append("),\n");
	}

	private static void addArrayWriter(final StringBuilder code, final String className, final AttributeInfo attr, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		final String converter = findConverter(attr, structs);
		String inline = allowInline ? InlinedEncoders.get(actualType) : null;
		code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createArrayEncoder(");
		if (attr.readMethod != null) code.append(className).append("::").append(attr.readMethod.getSimpleName());
		else code.append("c -> c.").append(attr.field.getSimpleName());
		code.append(", ");
		if (converter != null) code.append(converter).append(".JSON_WRITER),\n");
		else if (inline != null) code.append(inline).append("),\n");
		else code.append("json ,").append(typeOrClass(objectType, attr.type.toString())).append("),\n");
	}

	private static void addAttributeReader(final StringBuilder code, final String className, final AttributeInfo attr, final String alias, final String readValue, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		final String inline = allowInline ? InlinedDecoders.get(actualType) : null;
		final String converter = findConverter(attr, structs);
		code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createDecoder(");
		code.append(readValue);
		code.append(", \"").append(alias).append("\", json, ");
		if (attr.fullMatch) code.append("true, ");
		else code.append("false, ");
		if (attr.mandatory) code.append("true, ");
		else code.append("false, ");
		code.append(attr.index).append(", ");
		if (converter != null) code.append(converter).append(".JSON_READER),\n");
		else if (inline != null) code.append(inline).append("),\n");
		else code.append(typeOrClass(objectType, attr.type.toString())).append("),\n");
	}

	private static void addArrayReader(final StringBuilder code, final String className, final AttributeInfo attr, final String readValue, final Map<String, StructInfo> structs, final boolean allowInline) {
		final String actualType = attr.type.toString();
		final String objectType = nonGenericObject(actualType);
		final String inline = allowInline ? InlinedDecoders.get(actualType) : null;
		final String converter = findConverter(attr, structs);
		code.append("\t\t\t\tcom.dslplatform.json.runtime.Settings.<");
		code.append(className).append(", ").append(objectType).append(">createArrayDecoder(");
		code.append(readValue);
		code.append(", ");
		if (converter != null) code.append(converter).append(".JSON_READER),\n");
		else if (inline != null) code.append(inline).append("),\n");
		else code.append("json, ").append(typeOrClass(objectType, attr.type.toString())).append("),\n");
	}
}
