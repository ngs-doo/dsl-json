package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static com.dslplatform.json.processor.CompiledJsonAnnotationProcessor.findConverterName;
import static com.dslplatform.json.processor.Context.nonGenericObject;
import static com.dslplatform.json.processor.Context.sortedAttributes;
import static com.dslplatform.json.processor.Context.typeOrClass;

class ConverterTemplate {

	private final Writer code;
	private final Context context;
	private final EnumTemplate enumTemplate;

	ConverterTemplate(Context context, EnumTemplate enumTemplate) {
		this.code = context.code;
		this.context = context;
		this.enumTemplate = enumTemplate;
	}

	private boolean isStaticEnum(AttributeInfo attr) {
		if (!attr.isEnum(context.structs)) return false;
		StructInfo target = context.structs.get(attr.typeName);
		return target != null && enumTemplate.isStatic(target);
	}

	void factoryForGenericConverter(final StructInfo si) throws IOException {
		String typeName = si.element.getQualifiedName().toString();
		String producedType;
		if (si.formats.contains(CompiledJson.Format.OBJECT)) {
			if (si.formats.contains(CompiledJson.Format.ARRAY)) {
				producedType = "com.dslplatform.json.runtime.FormatDescription";
			} else {
				producedType = "ObjectFormatConverter";
			}
		} else {
			producedType = "ArrayFormatConverter";
		}

		code.append("\tprivate final static class ConverterFactory implements com.dslplatform.json.DslJson.ConverterFactory<");
		code.append(producedType);
		code.append("> {\n");
		code.append("\t\t@Override\n");
		code.append("\t\tpublic ").append(producedType).append(" tryCreate(java.lang.reflect.Type manifest, com.dslplatform.json.DslJson __dsljson) {\n");
		code.append("\t\t\tif (manifest instanceof java.lang.reflect.ParameterizedType) {\n");
		code.append("\t\t\t\tjava.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) manifest;\n");
		code.append("\t\t\t\tjava.lang.Class<?> rawClass = (java.lang.Class<?>) pt.getRawType();\n");
		code.append("\t\t\t\tif (rawClass.isAssignableFrom(").append(typeName).append(".class)) {\n");
		createConverter(si, typeName, "pt.getActualTypeArguments()");
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t} else if (").append(typeName).append(".class.equals(manifest)) {\n");
		code.append("\t\t\t\tjava.lang.reflect.Type[] unknownArgs = new java.lang.reflect.Type[");
		code.append(Integer.toString(si.typeParametersNames.size())).append("];\n");
		code.append("\t\t\t\tjava.util.Arrays.fill(unknownArgs, Object.class);\n");
		code.append("\t\t\t\tif (__dsljson.tryFindReader(Object.class) != null && __dsljson.tryFindWriter(Object.class) != null) {\n");
		createConverter(si, typeName, "unknownArgs");
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\treturn null;\n");
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	private void createConverter(StructInfo si, String typeName, String typeArguments) throws IOException {
		if (si.formats.contains(CompiledJson.Format.OBJECT)) {
			if (si.formats.contains(CompiledJson.Format.ARRAY)) {
				code.append("\t\t\t\t\treturn new com.dslplatform.json.runtime.FormatDescription(\n");
				code.append("\t\t\t\t\t\t\t").append(typeName).append(".class,\n");
				code.append("\t\t\t\t\t\t\tnew ObjectFormatConverter(__dsljson, ").append(typeArguments).append("),\n");
				code.append("\t\t\t\t\t\t\tnew ArrayFormatConverter(__dsljson, ").append(typeArguments).append("),\n");
				code.append("\t\t\t\t\t\t\t").append(String.valueOf(si.isObjectFormatFirst)).append(",\n");
				String typeAlias = si.deserializeName.isEmpty() ? typeName : si.deserializeName;
				code.append("\t\t\t\t\t\t\t\"").append(typeAlias).append("\",\n");
				code.append("\t\t\t\t\t\t\t__dsljson);\n");
			} else {
				code.append("\t\t\t\t\treturn new ObjectFormatConverter(__dsljson, ").append(typeArguments).append(");\n");
			}
		} else {
			code.append("\t\t\t\t\treturn new ArrayFormatConverter(__dsljson, ").append(typeArguments).append(");\n");
		}
	}

	private void asFormatConverter(final StructInfo si, final String name, final String className, final boolean binding) throws IOException {
		code.append("\tpublic final static class ").append(name);
		if (si.isParameterized) {
			code.append('<');
			final List<? extends TypeParameterElement> params = si.element.getTypeParameters();
			for (int i = 0; i < params.size(); i++) {
				if (i > 0) {
					code.append(", ");
				}
				final TypeParameterElement tpe = params.get(i);
				code.append(tpe.getSimpleName().toString());
				if (!tpe.getBounds().isEmpty()) {
					code.append(" extends ");
					code.append(tpe.getBounds().get(0).toString());
					for (int x = 1; x < tpe.getBounds().size(); x++) {
						code.append(", ");
						code.append(tpe.getBounds().get(x).toString());
					}
				}
			}
			code.append('>');
		}
		code.append(" implements com.dslplatform.json.runtime.FormatConverter<");
		if (binding) {
			code.append(className).append(">, com.dslplatform.json.JsonReader.BindObject<");
		}
		code.append(className).append("> {\n");
		code.append("\t\tprivate final boolean alwaysSerialize;\n");
		code.append("\t\tprivate final com.dslplatform.json.DslJson __dsljson;\n");
		if (si.isParameterized) {
			code.append("\t\tprivate final java.lang.reflect.Type[] actualTypes;\n");
		}

		for (AttributeInfo attr : si.attributes.values()) {
			OptimizedConverter converter = context.inlinedConverters.get(attr.typeName);
			StructInfo target = context.structs.get(attr.typeName);
			if (attr.converter == null && (target == null || target.converter == null) && converter == null && !isStaticEnum(attr) && !attr.isJsonObject) {
				List<String> types = attr.collectionContent(context.typeSupport, context.structs);
				if (target != null && attr.isEnum(context.structs)) {
					code.append("\t\tprivate final ").append(findConverterName(target)).append(".EnumConverter converter_").append(attr.name).append(";\n");
				} else if (types != null && types.size() == 1 || (attr.isGeneric && !attr.containsStructOwnerType)) {
					String content = extractSingleType(attr, types);
					TypeMirror mirror = context.useLazyResolution(content) ? context.findType(content) : null;
					if (mirror != null) {
						createLazyReaderAndWriter(attr, mirror, "", si);
					} else {
						String objectType = Analysis.objectName(content);
						code.append("\t\tprivate final com.dslplatform.json.JsonReader.ReadObject<").append(objectType).append("> reader_").append(attr.name).append(";\n");
						code.append("\t\tprivate final com.dslplatform.json.JsonWriter.WriteObject<").append(objectType).append("> writer_").append(attr.name).append(";\n");
					}
				} else if (types != null && types.size() == 2) {
					TypeMirror keyMirror = context.useLazyResolution(types.get(0)) ? context.findType(types.get(0)) : null;
					if (keyMirror != null) {
						createLazyReaderAndWriter(attr, keyMirror, "key_", si);
					} else {
						String objectType = Analysis.objectName(types.get(0));
						code.append("\t\tprivate final com.dslplatform.json.JsonReader.ReadObject<").append(objectType).append("> key_reader_").append(attr.name).append(";\n");
						code.append("\t\tprivate final com.dslplatform.json.JsonWriter.WriteObject<").append(objectType).append("> key_writer_").append(attr.name).append(";\n");
					}
					TypeMirror valueMirror = context.useLazyResolution(types.get(1)) ? context.findType(types.get(1)) : null;
					if (valueMirror != null) {
						createLazyReaderAndWriter(attr, valueMirror, "value_", si);
					} else {
						String objectType = Analysis.objectName(types.get(1));
						code.append("\t\tprivate final com.dslplatform.json.JsonReader.ReadObject<").append(objectType).append("> value_reader_").append(attr.name).append(";\n");
						code.append("\t\tprivate final com.dslplatform.json.JsonWriter.WriteObject<").append(objectType).append("> value_writer_").append(attr.name).append(";\n");
					}
				} else {
					createLazyReaderAndWriter(attr, attr.type, "", si);
				}
				if (attr.isArray) {
					String content = context.extractRawType(((ArrayType) attr.type).getComponentType(), si.genericSignatures);
					code.append("\t\tprivate final ").append(content).append("[] emptyArray_").append(attr.name).append(";\n");
				}
			} else if (converter != null && attr.isArray && attr.notNull) {
				if (converter.defaultValue != null) {
					code.append("\t\tprivate static final ").append(attr.typeName).append(" emptyArray_").append(attr.name);
					code.append(" = ").append(converter.defaultValue).append(";\n");
				} else {
					String content = context.extractRawType(((ArrayType) attr.type).getComponentType(), si.genericSignatures);
					code.append("\t\tprivate final ").append(content).append("[] emptyArray_").append(attr.name).append(";\n");
				}
			}
		}
		code.append("\t\tpublic ").append(name).append("(com.dslplatform.json.DslJson __dsljson");
		if (si.isParameterized) {
			code.append(", java.lang.reflect.Type[] actualTypes");
		}
		code.append(") {\n");

		switch (si.objectFormatPolicy) {
			case DEFAULT:
			case EXPLICIT:
				code.append("\t\t\tthis.alwaysSerialize = !__dsljson.omitDefaults;\n");
				break;
			case MINIMAL:
				code.append("\t\t\tthis.alwaysSerialize = false;\n");
				break;
			case FULL:
				code.append("\t\t\tthis.alwaysSerialize = true;\n");
				break;
		}

		code.append("\t\t\tthis.__dsljson = __dsljson;\n");
		if (si.isParameterized) {
			code.append("\t\t\tthis.actualTypes = actualTypes;\n");
		}

		for (AttributeInfo attr : si.attributes.values()) {
			boolean hasConverter = context.inlinedConverters.containsKey(attr.typeName);
			List<String> types = attr.collectionContent(context.typeSupport, context.structs);
			StructInfo target = context.structs.get(attr.typeName);
			if (attr.converter == null && (target == null || target.converter == null) && !hasConverter && !isStaticEnum(attr) && !attr.isJsonObject) {
				if (target != null && attr.isEnum(context.structs)) {
					code.append("\t\t\tthis.converter_").append(attr.name).append(" = new ").append(findConverterName(target)).append(".EnumConverter(__dsljson);\n");
				} else if (types != null && types.size() == 1) {
					String content = types.get(0);
					OptimizedConverter converter = context.inlinedConverters.get(content);
					if (converter != null) {
						code.append("\t\t\tthis.reader_").append(attr.name).append(" = ").append(converter.decoderField).append(";\n");
						code.append("\t\t\tthis.writer_").append(attr.name).append(" = ").append(converter.encoderField).append(";\n");
					} else if (!context.useLazyResolution(content)) {
						String type = typeOrClass(nonGenericObject(content), content);
						code.append("\t\t\tthis.reader_").append(attr.name).append(" = __dsljson.tryFindReader(").append(type).append(");\n");
						code.append("\t\t\tthis.writer_").append(attr.name).append(" = __dsljson.tryFindWriter(").append(type).append(");\n");
					}
				} else if (types != null && types.size() == 2) {
					OptimizedConverter converterKey = context.inlinedConverters.get(types.get(0));
					if (converterKey != null) {
						code.append("\t\t\tthis.key_reader_").append(attr.name).append(" = ").append(converterKey.decoderField).append(";\n");
						code.append("\t\t\tthis.key_writer_").append(attr.name).append(" = ").append(converterKey.encoderField).append(";\n");
					} else if (!context.useLazyResolution(types.get(0))) {
						String typeKey = typeOrClass(nonGenericObject(types.get(0)), types.get(0));
						code.append("\t\t\tthis.key_reader_").append(attr.name).append(" = __dsljson.tryFindReader(").append(typeKey).append(");\n");
						code.append("\t\t\tthis.key_writer_").append(attr.name).append(" = __dsljson.tryFindWriter(").append(typeKey).append(");\n");
					}
					OptimizedConverter converterValue = context.inlinedConverters.get(types.get(1));
					if (converterValue != null) {
						code.append("\t\t\tthis.value_reader_").append(attr.name).append(" = ").append(converterValue.decoderField).append(";\n");
						code.append("\t\t\tthis.value_writer_").append(attr.name).append(" = ").append(converterValue.encoderField).append(";\n");
					} else if (!context.useLazyResolution(types.get(1))) {
						String typeValue = typeOrClass(nonGenericObject(types.get(1)), types.get(1));
						code.append("\t\t\tthis.value_reader_").append(attr.name).append(" = __dsljson.tryFindReader(").append(typeValue).append(");\n");
						code.append("\t\t\tthis.value_writer_").append(attr.name).append(" = __dsljson.tryFindWriter(").append(typeValue).append(");\n");
					}
				} else if (attr.isGeneric && !attr.containsStructOwnerType) {
					String type;
					if (attr.isArray) {
						type = createTypeSignature(((ArrayType) attr.type).getComponentType(), attr.typeVariablesIndex, si.genericSignatures);
					} else {
						type = createTypeSignature(attr.type, attr.typeVariablesIndex, si.genericSignatures);
					}

					code.append("\t\t\tjava.lang.reflect.Type manifest_").append(attr.name).append(" = ").append(type).append(";\n");
					code.append("\t\t\tthis.reader_").append(attr.name).append(" = __dsljson.tryFindReader(manifest_").append(attr.name).append(");\n");
					code.append("\t\t\tif (reader_").append(attr.name).append(" == null) {\n");
					code.append("\t\t\t\tthrow new com.dslplatform.json.ConfigurationException(\"Unable to find reader for \" + manifest_").append(attr.name);
					code.append(" + \". Enable runtime conversion by initializing DslJson with new DslJson<>(Settings.withRuntime().includeServiceLoader())\");\n");
					code.append("\t\t\t}\n");

					code.append("\t\t\tthis.writer_").append(attr.name).append(" = __dsljson.tryFindWriter(manifest_").append(attr.name).append(");\n");
					code.append("\t\t\tif (writer_").append(attr.name).append(" == null) {\n");
					code.append("\t\t\t\tthrow new com.dslplatform.json.ConfigurationException(\"Unable to find writer for \" + manifest_").append(attr.name);
					code.append(" + \". Enable runtime conversion by initializing DslJson with new DslJson<>(Settings.withRuntime().includeServiceLoader())\");\n");
					code.append("\t\t\t}\n");
				}
				if (attr.isArray) {
					TypeMirror arrayComponentType = ((ArrayType) attr.type).getComponentType();
					code.append("\t\t\tthis.emptyArray_").append(attr.name).append(" = ");
					String content = context.extractRawType(arrayComponentType, si.genericSignatures);
					code.append("(").append(content).append("[]) java.lang.reflect.Array.newInstance((Class<?>) ");
					buildArrayType(arrayComponentType, attr.typeVariablesIndex, si.genericSignatures);
					code.append(", 0);\n");
				}
			}
		}
		code.append("\t\t}\n");
		if (binding) {
			code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
			code.append("\t\t\tif (reader.wasNull()) return null;\n");
			code.append("\t\t\treturn bind(reader, ");
			if (si.annotatedFactory != null) {
				code.append(si.annotatedFactory.getEnclosingElement().toString()).append(".").append(si.annotatedFactory.getSimpleName()).append("());\n");
			} else {
				code.append("new ").append(className).append("());\n");
			}
			code.append("\t\t}\n");
		}
	}

	private String extractTypeSignature(AttributeInfo attr, TypeMirror type, Map<String, TypeMirror> genericSignatures) {
		if (attr.isGeneric || !attr.usedTypes.isEmpty()) {
			return createTypeSignature(type, attr.typeVariablesIndex, genericSignatures);
		}
		String typeName = Analysis.unpackType(type, context.types()).toString();
		return typeOrClass(nonGenericObject(typeName), typeName);
	}

	private String extractSingleType(AttributeInfo attr, @Nullable List<String> types) {
		if (types == null || attr.isGeneric) {
			if (attr.isArray) {
				return ((ArrayType) attr.type).getComponentType().toString();
			}
			return attr.typeName;
		}
		return types.get(0);
	}

	private void createLazyReaderAndWriter(AttributeInfo attr, TypeMirror mirror, String namePrefix, StructInfo si) throws IOException {
		String type = extractTypeSignature(attr, mirror, si.genericSignatures);
		String typeName = Analysis.objectName(attr.createTypeSignature(context.types(), mirror, si.genericSignatures));
		code.append("\t\tprivate com.dslplatform.json.JsonReader.ReadObject<").append(typeName).append("> ").append(namePrefix).append("reader_").append(attr.name).append(";\n");
		code.append("\t\tprivate com.dslplatform.json.JsonReader.ReadObject<").append(typeName).append("> ").append(namePrefix).append("reader_").append(attr.name).append("() {\n");
		code.append("\t\t\tif (").append(namePrefix).append("reader_").append(attr.name).append(" == null) {\n");
		code.append("\t\t\t\tjava.lang.reflect.Type manifest = ").append(type).append(";\n");
		code.append("\t\t\t\t").append(namePrefix).append("reader_").append(attr.name).append(" = __dsljson.tryFindReader(manifest);\n");
		code.append("\t\t\t\tif (").append(namePrefix).append("reader_").append(attr.name).append(" == null) {\n");
		code.append("\t\t\t\t\tthrow new com.dslplatform.json.ConfigurationException(\"Unable to find reader for \" + manifest + \". Enable runtime conversion by initializing DslJson with new DslJson<>(Settings.basicSetup())\");\n");
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\treturn ").append(namePrefix).append("reader_").append(attr.name).append(";\n");
		code.append("\t\t}\n");

		code.append("\t\tprivate com.dslplatform.json.JsonWriter.WriteObject<").append(typeName).append("> ").append(namePrefix).append("writer_").append(attr.name).append(";\n");
		code.append("\t\tprivate com.dslplatform.json.JsonWriter.WriteObject<").append(typeName).append("> ").append(namePrefix).append("writer_").append(attr.name).append("() {\n");
		code.append("\t\t\tif (").append(namePrefix).append("writer_").append(attr.name).append(" == null) {\n");
		code.append("\t\t\t\tjava.lang.reflect.Type manifest = ").append(type).append(";\n");
		code.append("\t\t\t\t").append(namePrefix).append("writer_").append(attr.name).append(" = __dsljson.tryFindWriter(manifest);\n");
		code.append("\t\t\t\tif (").append(namePrefix).append("writer_").append(attr.name).append(" == null) {\n");
		code.append("\t\t\t\t\tthrow new com.dslplatform.json.ConfigurationException(\"Unable to find writer for \" + manifest + \". Enable runtime conversion by initializing DslJson with new DslJson<>(Settings.basicSetup())\");\n");
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\treturn ").append(namePrefix).append("writer_").append(attr.name).append(";\n");
		code.append("\t\t}\n");
	}

	private String createTypeSignature(
			TypeMirror type,
			Map<String, Integer> typeVariableIndexes,
			Map<String, TypeMirror> genericSignatures) {
		StringBuilder builder = new StringBuilder();
		createTypeSignature(type, typeVariableIndexes, genericSignatures, builder);
		return builder.toString();
	}

	private void createTypeSignature(
			TypeMirror type,
			Map<String, Integer> typeVariableIndexes,
			Map<String, TypeMirror> genericSignatures,
			StringBuilder builder) {
		String typeName = Analysis.unpackType(type, context.types()).toString();
		if (type.getKind() == TypeKind.DECLARED) {
			DeclaredType declaredType = (DeclaredType) type;
			if (declaredType.getTypeArguments().isEmpty()) {
				builder.append(typeName).append(".class");
			} else {
				TypeElement typeElement = (TypeElement) declaredType.asElement();
				builder.append("com.dslplatform.json.runtime.Generics.makeParameterizedType(").append(typeElement.getQualifiedName()).append(".class");
				for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
					builder.append(", ");
					createTypeSignature(typeArgument, typeVariableIndexes, genericSignatures, builder);
				}
				builder.append(")");
			}
			return;
		}
		if (type.getKind() == TypeKind.ARRAY) {
			ArrayType arrayType = (ArrayType) type;
			builder.append("com.dslplatform.json.runtime.Generics.makeArrayType(");
			createTypeSignature(arrayType.getComponentType(), typeVariableIndexes, genericSignatures, builder);
			builder.append(")");
			return;
		}
		if (type instanceof WildcardType) {
			WildcardType wt = (WildcardType) type;
			createTypeSignature(wt.getExtendsBound(), typeVariableIndexes, genericSignatures, builder);
			return;
		}
		//TODO: not sure if there are some shenanigans with type signatures, so try the original one first
		if (typeVariableIndexes.containsKey(type.toString())) {
			Integer index = typeVariableIndexes.get(type.toString());
			if (index != null && index >= 0) {
				builder.append("actualTypes[").append(index).append("]");
				return;
			}
		}
		if (type instanceof TypeVariable) {
			if (typeVariableIndexes.containsKey(typeName)) {
				Integer index = typeVariableIndexes.get(typeName);
				if (index != null && index >= 0) {
					builder.append("actualTypes[").append(index).append("]");
					return;
				}
			}
			TypeMirror mirror = genericSignatures.get(typeName);
			if (mirror != null && mirror != type) {
				createTypeSignature(mirror, typeVariableIndexes, genericSignatures, builder);
				return;
			}
		}
		builder.append(typeName).append(".class");
	}

	private void buildArrayType(
			TypeMirror type,
			Map<String, Integer> typeVariableIndexes,
			Map<String, TypeMirror> genericSignatures) throws IOException {
		String typeName = Analysis.unpackType(type, context.types()).toString();
		if (type.getKind() == TypeKind.DECLARED) {
			DeclaredType declaredType = (DeclaredType) type;
			if (declaredType.getTypeArguments().isEmpty()) {
				code.append(typeName);
			} else {
				int first = typeName.indexOf('<');
				code.append(typeName, 0, first);
			}
			code.append(".class");
			return;
		}
		if (type.getKind() == TypeKind.ARRAY) {
			ArrayType arrayType = (ArrayType) type;
			code.append("com.dslplatform.json.runtime.Generics.makeArrayType(");
			buildArrayType(arrayType.getComponentType(), typeVariableIndexes, genericSignatures);
			code.append(")");
			return;
		}
		if (typeVariableIndexes.containsKey(type.toString())) {
			Integer index = typeVariableIndexes.get(type.toString());
			if (index != null && index >= 0) {
				code.append("actualTypes[").append(Integer.toString(index)).append("]");
				return;
			}
		}
		if (type instanceof TypeVariable) {
			if (typeVariableIndexes.containsKey(typeName)) {
				Integer index = typeVariableIndexes.get(typeName);
				if (index != null && index >= 0) {
					code.append("actualTypes[").append(Integer.toString(index)).append("]");
					return;
				}
			}
			TypeMirror mirror = genericSignatures.get(typeName);
			if (mirror != null) {
				buildArrayType(mirror, typeVariableIndexes, genericSignatures);
				return;
			}
		}
		code.append(typeName).append(".class");
	}

	void emptyObject(final StructInfo si, String className) throws IOException {
		asFormatConverter(si, "ObjectFormatConverter", className, true);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si, true);
		writeObject(si, className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" bind(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.last() != '{') throw reader.newParseError(\"Expecting '{' for object start\");\n");
		code.append("\t\t\treader.getNextToken();\n");
		code.append("\t\t\tbindContent(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\t").append(className).append(" instance = ");
		if (si.annotatedFactory != null) {
			code.append(si.annotatedFactory.getEnclosingElement().toString()).append(".").append(si.annotatedFactory.getSimpleName()).append("();\n ");
		} else {
			code.append("new ").append(className).append("();\n ");
		}
		code.append("\t\t\tbindContent(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic void bindContent(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		for (int i = 0; i < sortedAttributes.size(); i++) {
			AttributeInfo attr = sortedAttributes.get(i);
			if (!attr.canReadInput() || attr.converter == null || attr.converter.defaultValue == null) continue;
			code.append("\t\t\tinstance.");
			if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
			else if (attr.writeMethod != null) code.append(attr.writeMethod.getSimpleName()).append("(");
			code.append(attr.converter.defaultValue);
			if (attr.writeMethod != null) code.append(")");
			code.append(";\n");
		}
		code.append("\t\t\tif (reader.last() == '}')");
		checkMandatory(sortedAttributes, 0);
		for (int i = 0; i < sortedAttributes.size(); i++) {
			AttributeInfo attr = sortedAttributes.get(i);
			if (!attr.canReadInput()) continue;
			String sn = si.serializedNames.get(attr.id);
			if (i > 0) {
				code.append("\t\t\tif (reader.getNextToken() == '}') ");
				checkMandatory(sortedAttributes, i);
				code.append("\t\t\tif (reader.last() != ',') throw reader.newParseError(\"Expecting ',' for other mandatory properties\"); else reader.getNextToken();\n");
			}
			code.append("\t\t\tif (reader.fillNameWeakHash() != ").append(Integer.toString(calcWeakHash(sn != null ? sn : attr.id)));
			code.append(" || !reader.wasLastName(name_").append(attr.name).append(")) { bindSlow(reader, instance, ");
			code.append(Integer.toString(i)).append("); return; }\n");
			code.append("\t\t\treader.getNextToken();\n");
			processPropertyValue(attr, "\t", true, si.genericSignatures);
		}
		if (si.onUnknown == CompiledJson.Behavior.FAIL) {
			if (si.discriminator.length() > 0 && !si.attributes.containsKey(si.discriminator)) {
				code.append("\t\t\tif (reader.getNextToken() == '}') return;\n");
				if (si.attributes.isEmpty()) {
					code.append("\t\t\tif (reader.last() == '\"') {\n");
				} else {
					code.append("\t\t\tif (reader.last() == ',') {\n");
					code.append("\t\t\t\treader.getNextToken();\n");
				}
				code.append("\t\t\t\treader.fillNameWeakHash();\n");
				code.append("\t\t\t\tbindSlow(reader, instance, ").append(Integer.toString(sortedAttributes.size())).append(");\n");
				code.append("\t\t\t\treturn;\n");
				code.append("\t\t\t}\n");
				code.append("\t\t\tthrow reader.newParseError(\"Expecting '}' for object end since unknown properties are not allowed on ");
				code.append(className).append("\");\n");
			} else {
				code.append("\t\t\tif (reader.getNextToken() != '}') throw reader.newParseError(\"Expecting '}' for object end since unknown properties are not allowed on ");
				code.append(className).append("\");\n");
			}
		} else {
			boolean hasDiscriminator = si.discriminator.length() > 0 && !si.attributes.containsKey(si.discriminator) && si.attributes.isEmpty();
			boolean hasProperties = !si.attributes.isEmpty();
			if (hasDiscriminator) {
				code.append("\t\t\tif (reader.last() == '\"') {\n");
			} else {
				if (hasProperties) {
					code.append("\t\t\tif (reader.getNextToken() != '}') {\n");
					code.append("\t\t\t\tif (reader.last() == ',') {\n");
					code.append("\t\t\t\t\treader.getNextToken();\n");
				} else {
					code.append("\t\t\tif (reader.last() != '\"') throw reader.newParseError(\"Expecting '}' for object end or '\\\"' for attribute start\");\n");
					code.append("\t\t\treader.fillNameWeakHash();\n");
					code.append("\t\t\tbindSlow(reader, instance, 0);\n");
				}
			}
			if (hasDiscriminator || hasProperties) {
				code.append("\t\t\t\t\treader.fillNameWeakHash();\n");
				code.append("\t\t\t\t\tbindSlow(reader, instance, ").append(Integer.toString(sortedAttributes.size())).append(");\n");
				code.append("\t\t\t\t}\n");
				code.append("\t\t\t\tif (reader.last() != '}') throw reader.newParseError(\"Expecting '}' for object end\");\n");
				if (!hasDiscriminator) {
					code.append("\t\t\t}\n");
				}
			}
		}
		code.append("\t\t}\n");
		code.append("\t\tprivate void bindSlow(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance, int index) throws java.io.IOException {\n");
		for (int i = 0; i < sortedAttributes.size(); i++) {
			AttributeInfo attr = sortedAttributes.get(i);
			if (!attr.canReadInput()) continue;
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			if (attr.mandatory || attr.notNull && nonPrimitive) {
				code.append("\t\t\tboolean __detected_").append(attr.name).append("__ = index > ").append(Integer.toString(i)).append(";\n");
			}
		}
		code.append("\t\t\tswitch(reader.getLastHash()) {\n");
		handleSwitch(si, "\t\t\t", true);
		code.append("\t\t\t}\n");
		if (sortedAttributes.isEmpty()) {
			code.append("\t\t}\n");
			code.append("\t}\n");
			return;
		}
		code.append("\t\t\twhile (reader.last() == ','){\n");
		code.append("\t\t\t\treader.getNextToken();\n");
		code.append("\t\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t\t", true);
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\tif (reader.last() != '}') throw reader.newParseError(\"Expecting '}' for object end\");\n");
		for (AttributeInfo attr : sortedAttributes) {
			if (!attr.canReadInput()) continue;
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			String defaultValue = context.getDefault(attr);
			if (attr.isArray && attr.notNull) {
				defaultValue = "emptyArray_" + attr.name;
			}
			if (attr.mandatory || attr.notNull && nonPrimitive && "null".equals(defaultValue)) {
				code.append("\t\t\tif (!__detected_").append(attr.name).append("__) throw reader.newParseErrorAt(\"Property '");
				code.append(attr.name).append("' is ");
				if (attr.mandatory) code.append("mandatory");
				else code.append("not-nullable and doesn't have a default");
				code.append(" but was not found in JSON\", 0);\n");
			} else if (attr.notNull && nonPrimitive && (attr.field != null || attr.writeMethod != null)) {
				code.append("\t\t\tif (!__detected_").append(attr.name).append("__ && instance.");
				code.append(attr.readProperty).append(" == null) {\n");
				code.append("\t\t\t\tinstance.");
				if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ").append(defaultValue).append(";\n");
				else code.append(attr.writeMethod.getSimpleName()).append("(").append(defaultValue).append(");\n");
				code.append("\t\t\t}\n");
			}
		}
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	void fromObject(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si, "ObjectFormatConverter", className, false);
		writeObject(si, className, sortedAttributes(si, true));
		List<AttributeInfo> sortedAttributes = sortedAttributes(si, false);
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.wasNull()) return null;\n");
		code.append("\t\t\telse if (reader.last() != '{') throw reader.newParseError(\"Expecting '{' for object start\");\n");
		code.append("\t\t\treader.getNextToken();\n");
		code.append("\t\t\treturn readContent(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\t").append(attr.typeName).append(" _").append(attr.name).append("_ = ");
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			String defaultValue = context.getDefault(attr);
			if (attr.isArray && attr.notNull) {
				code.append("emptyArray_").append(attr.name);
			} else if (context.isObjectInstance(attr)) {
				code.append("null");
			} else {
				code.append(context.getDefault(attr));
			}
			code.append(";\n");
			if (attr.mandatory || attr.notNull && nonPrimitive && ("null".equals(defaultValue) || context.isObjectInstance(attr))) {
				code.append("\t\t\tboolean __detected_").append(attr.name).append("__ = false;\n");
			}
		}
		code.append("\t\t\tif (reader.last() == '}') {\n");
		checkMandatory(sortedAttributes, "\t\t\t\t");
		returnInstance("\t\t\t\t", si, className);
		code.append("\t\t\t}\n");
		code.append("\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t", false);
		code.append("\t\t\t}\n");
		code.append("\t\t\twhile (reader.last() == ','){\n");
		code.append("\t\t\t\treader.getNextToken();\n");
		code.append("\t\t\t\tswitch(reader.fillName()) {\n");
		handleSwitch(si, "\t\t\t\t", false);
		code.append("\t\t\t\t}\n");
		code.append("\t\t\t}\n");
		code.append("\t\t\tif (reader.last() != '}') throw reader.newParseError(\"Expecting '}' for object end\");\n");
		checkMandatory(sortedAttributes, "\t\t\t");
		returnInstance("\t\t\t", si, className);
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	private void writeDiscriminator(final StructInfo si) throws IOException {
		String name = si.deserializeName.isEmpty() ? si.binaryName.replace('$', '.') : si.deserializeName;
		code.append("\t\t\t\twriter.writeAscii(\"\\\"").append(si.discriminator).append("\\\":\\\"").append(name).append("\\\"");
		if (!si.attributes.isEmpty()) {
			code.append(",");
		}
		code.append("\");\n");
	}

	private void writeObject(final StructInfo si, final String className, List<AttributeInfo> sortedAttributes) throws IOException {
		boolean isFirst = true;
		for (AttributeInfo attr : sortedAttributes) {
			if (!attr.canWriteOutput()) continue;
			String prefix = isFirst ? "" : ",";
			isFirst = false;
			String name = si.propertyName(attr);
			code.append("\t\tprivate static final byte[] quoted_").append(attr.name).append(" = \"").append(prefix);
			code.append("\\\"").append(name).append("\\\":\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n");
			code.append("\t\tprivate static final byte[] name_").append(attr.name).append(" = \"").append(name).append("\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n");
		}
		code.append("\t\tpublic final void write(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		code.append("\t\t\tif (instance == null) writer.writeNull();\n");
		code.append("\t\t\telse {\n");
		code.append("\t\t\t\twriter.writeByte((byte)'{');\n");
		if (si.discriminator.length() > 0 && !si.attributes.containsKey(si.discriminator)) {
			writeDiscriminator(si);
			if (!si.attributes.isEmpty()) {
				code.append("\t\t\t\tif (alwaysSerialize) { writeContentFull(writer, instance); writer.writeByte((byte)'}'); }\n");
				code.append("\t\t\t\telse { writeContentMinimal(writer, instance); writer.getByteBuffer()[writer.size() - 1] = '}'; }\n");
			} else {
				code.append("\t\t\t\twriter.writeByte((byte)'}');\n");
			}
		} else {
			code.append("\t\t\t\tif (alwaysSerialize) { writeContentFull(writer, instance); writer.writeByte((byte)'}'); }\n");
			code.append("\t\t\t\telse if (writeContentMinimal(writer, instance)) writer.getByteBuffer()[writer.size() - 1] = '}';\n");
			code.append("\t\t\t\telse writer.writeByte((byte)'}');\n");
		}
		code.append("\t\t\t}\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic void writeContentFull(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		for (AttributeInfo attr : sortedAttributes) {
			if (!attr.canWriteOutput()) continue;
			code.append("\t\t\twriter.writeAscii(quoted_").append(attr.name).append(");\n");
			writeProperty(attr, false, "\t\t\t");
		}
		code.append("\t\t}\n");

		code.append("\t\tpublic boolean writeContentMinimal(final com.dslplatform.json.JsonWriter writer, final ");
		code.append(className).append(" instance) {\n");
		code.append("\t\t\tboolean hasWritten = false;\n");
		for (AttributeInfo attr : sortedAttributes) {
			if (!attr.canWriteOutput()) continue;
			String defaultValue = context.getDefault(attr);

			boolean checkDefaults = attr.includeToMinimal != JsonAttribute.IncludePolicy.ALWAYS;
			boolean isPrimitive = !attr.typeName.equals(Analysis.objectName(attr.typeName));
			String readValue = "instance." + attr.readProperty;

			if (checkDefaults) {
				code.append("\t\t\tif (");
				if ("null".equals(defaultValue) || isPrimitive) {
					code.append(readValue).append(" != ").append(defaultValue);
				} else if (attr.notNull && attr.isArray) {
					code.append(readValue).append(" != null && ").append(readValue).append(".length != 0");
				} else if (attr.notNull && (attr.isList || attr.isSet || attr.isMap)) {
					code.append(readValue).append(" != null && !").append(readValue).append(".isEmpty()");
				} else {
					StructInfo target = context.structs.get(attr.typeName);
					if (target != null && (target.hasEmptyCtor() || target.hasKnownConversion() || target.annotatedFactory != null)) {
						code.append(readValue).append(" != null");
					} else {
						code.append(readValue).append(" != null && !").append(defaultValue).append(".equals(").append(readValue).append(")");
					}
				}
				code.append(") {\n");
			}

			String alignment = checkDefaults ? "\t\t\t\t" : "\t\t\t";
			code.append(alignment).append("writer.writeByte((byte)'\"'); writer.writeAscii(name_").append(attr.name).append("); writer.writeByte((byte)'\"'); writer.writeByte((byte)':');\n");
			writeProperty(attr, checkDefaults, alignment);
			code.append(alignment).append("writer.writeByte((byte)','); hasWritten = true;\n");

			if (checkDefaults) {
				code.append("\t\t\t}");
				if (attr.notNull && !isPrimitive) {
					code.append(" else ");
					if (!"null".equals(defaultValue) || attr.isArray || attr.isList || attr.isSet || attr.isMap) {
						code.append("if (").append(readValue).append(" == null) ");
					}
					code.append("throw new com.dslplatform.json.ConfigurationException(\"Property '");
					code.append(attr.name).append("' is not allowed to be null\");\n");
				} else code.append("\n");
			}
		}
		code.append("\t\t\treturn hasWritten;\n");
		code.append("\t\t}\n");
	}

	private void checkMandatory(final List<AttributeInfo> attributes, final int start) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < attributes.size(); i++) {
			AttributeInfo attr = attributes.get(i);
			if (!attr.canReadInput()) continue;
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			String defaultValue = context.getDefault(attr);
			if (attr.mandatory || attr.notNull && nonPrimitive && "null".equals(defaultValue)) {
				int length = sb.length();
				if (!attr.mandatory) {
					sb.append("\t\t\t\tif (instance.").append(attr.readProperty).append(" == null)");
				}
				sb.append(" throw reader.newParseErrorAt(\"Property '").append(attr.name).append("' is ");
				if (attr.mandatory) sb.append("mandatory");
				else sb.append("not-nullable and doesn't have a default");
				sb.append(" but was not found in JSON\", 0);\n");
				if (attr.mandatory) {
					if (length != 0) code.append(" { \n");
					code.append(sb.toString());
					if (length != 0) code.append(" } ");
					return;
				}
			} else if (attr.notNull && nonPrimitive) {
				if (attr.isArray) {
					defaultValue = "emptyArray_" + attr.name;
				}
				if (!"null".equals(defaultValue) && (attr.field != null || attr.writeMethod != null)) {
					sb.append("\t\t\t\tif (instance.").append(attr.readProperty).append(" == null) instance.");
					if (attr.field != null) sb.append(attr.field.getSimpleName()).append(" = ").append(defaultValue).append(";\n");
					else sb.append(attr.writeMethod.getSimpleName()).append("(").append(defaultValue).append(");\n");
				}
			}
		}
		if (sb.length() > 0) {
			code.append(" {\n");
			code.append(sb.toString());
			code.append("\t\t\t\treturn;\n\t\t\t}\n");
		} else {
			code.append(" return;\n");
		}
	}

	private void checkMandatory(final List<AttributeInfo> attributes, String padding) throws IOException {
		for (AttributeInfo attr : attributes) {
			if (!attr.canReadInput()) continue;
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			String defaultValue = context.getDefault(attr);
			if (attr.mandatory || attr.notNull && nonPrimitive && ("null".equals(defaultValue) || context.isObjectInstance(attr))) {
				code.append(padding).append("if (!__detected_").append(attr.name).append("__) throw reader.newParseErrorAt(\"Property '");
				code.append(attr.name).append("' is ");
				if (attr.mandatory) code.append("mandatory");
				else code.append("not-nullable and doesn't have a default");
				code.append(" but was not found in JSON\", 0);\n");
			}
		}
	}

	void emptyArray(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si, "ArrayFormatConverter", className, true);
		List<AttributeInfo> sortedAttributes = sortedAttributes(si, true);
		writeArray(className, sortedAttributes);
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\t").append(className).append(" instance = ");
		if (si.annotatedFactory != null) {
			code.append(si.annotatedFactory.getEnclosingElement().toString()).append(".").append(si.annotatedFactory.getSimpleName()).append("();\n ");
		} else {
			code.append("new ").append(className).append("();\n ");
		}
		code.append("\t\t\tbind(reader, instance);\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" bind(final com.dslplatform.json.JsonReader reader, final ");
		code.append(className).append(" instance) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.last() != '[') throw reader.newParseError(\"Expecting '[' for object start\");\n");
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\treader.getNextToken();\n");
			processPropertyValue(attr, "\t", true, si.genericSignatures);
			i--;
			if (i > 0) {
				code.append("\t\t\tif (reader.getNextToken() != ',') throw reader.newParseError(\"Expecting ',' for other object elements\");\n");
			}
		}
		code.append("\t\t\tif (reader.getNextToken() != ']') throw reader.newParseError(\"Expecting ']' for object end\");\n");
		code.append("\t\t\treturn instance;\n");
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	void fromArray(final StructInfo si, final String className) throws IOException {
		asFormatConverter(si, "ArrayFormatConverter", className, false);
		writeArray(className, sortedAttributes(si, true));
		List<AttributeInfo> sortedAttributes = sortedAttributes(si, false);
		code.append("\t\tpublic ").append(className).append(" read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		code.append("\t\t\tif (reader.wasNull()) return null;\n");
		code.append("\t\t\telse if (reader.last() != '[') throw reader.newParseError(\"Expecting '[' for object start\");\n");
		code.append("\t\t\treturn readContent(reader);\n");
		code.append("\t\t}\n");
		code.append("\t\tpublic ").append(className).append(" readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {\n");
		int i = sortedAttributes.size();
		for (AttributeInfo attr : sortedAttributes) {
			code.append("\t\t\tfinal ").append(attr.typeName).append(" _").append(attr.name).append("_;\n");
			code.append("\t\t\treader.getNextToken();\n");
			processPropertyValue(attr, "\t", false, si.genericSignatures);
			i--;
			if (i > 0) {
				code.append("\t\t\tif (reader.getNextToken() != ',') throw reader.newParseError(\"Expecting ',' for other object elements\");\n");
			}
		}
		code.append("\t\t\tif (reader.getNextToken() != ']') throw reader.newParseError(\"Expecting ']' for object end\");\n");
		returnInstance("\t\t\t", si, className);
		code.append("\t\t}\n");
		code.append("\t}\n");
	}

	private void returnInstance(final String alignment, StructInfo info, final String className) throws IOException {
		code.append(alignment).append("return ");
		//builder can be invalid, so execute it only when other methods are not available
		if (info.annotatedFactory == null && info.selectedConstructor() == null && info.builder != null) {
			ExecutableElement factory = info.builder.factory;
			if (factory != null) {
				code.append(factory.getEnclosingElement().toString()).append(".").append(factory.getSimpleName()).append("()");
			} else {
				code.append("new ").append(info.builder.type.toString()).append("()");
			}
			for (AttributeInfo att : info.attributes.values()) {
				code.append(".").append(att.writeMethod.getSimpleName()).append("(_").append(att.name).append("_)");
			}
			code.append(".").append(info.builder.build.getSimpleName()).append("();\n");
			return;
		}
		final List<? extends VariableElement> params;
		if (info.annotatedFactory != null) {
			code.append(info.annotatedFactory.getEnclosingElement().toString()).append(".").append(info.annotatedFactory.getSimpleName()).append("(");
			params = info.annotatedFactory.getParameters();
		} else {
			code.append("new ").append(className).append("(");
			params = info.selectedConstructor().getParameters();
		}
		int i = params.size();
		for (VariableElement p : params) {
			VariableElement n = info.argumentMapping.get(p);
			code.append("_").append((n != null ? n : p).getSimpleName()).append("_");
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
			writeProperty(attr, false, "\t\t\t");
			i--;
			if (i > 0) code.append("\t\t\twriter.writeByte((byte)',');\n");
		}
		code.append("\t\t}\n");
	}

	private void writeProperty(AttributeInfo attr, boolean checkedDefault, String alignment) throws IOException {
		String readValue = "instance." + attr.readProperty;
		StructInfo target = context.structs.get(attr.typeName);
		String objectType = Analysis.objectName(attr.typeName);
		boolean canBeNull = !checkedDefault && objectType.equals(attr.typeName);
		if (attr.notNull && canBeNull) {
			code.append(alignment).append("if (").append(readValue);
			code.append(" == null) throw new com.dslplatform.json.ConfigurationException(\"Property '").append(attr.name).append("' is not allowed to be null\");\n");
			code.append(alignment);
		} else if (canBeNull) {
			code.append(alignment).append("if (").append(readValue).append(" == null) writer.writeNull();\n");
			code.append(alignment).append("else ");
		} else {
			code.append(alignment);
		}
		if (attr.converter != null) {
			attr.converter.write(code);
			code.append(".write(writer, ").append(readValue).append(");\n");
		} else if (attr.isJsonObject) {
			code.append(readValue).append(".serialize(writer, !alwaysSerialize);\n");
		} else if (target != null && target.converter != null) {
			target.converter.write(code);
			code.append(".write(writer, ").append(readValue).append(");\n");
		} else {
			OptimizedConverter optimizedConverter = context.inlinedConverters.get(attr.typeName);
			List<String> types = attr.collectionContent(context.typeSupport, context.structs);
			if (optimizedConverter != null) {
				code.append(optimizedConverter.nonNullableEncoder("writer", readValue)).append(";\n");
			} else if (target != null && attr.isEnum(context.structs)) {
				enumTemplate.writeName(code, target, readValue, "converter_" + attr.name);
			} else if (types != null) {
				if (attr.type.toString().equals(attr.typeName) || attr.isArray) {
					code.append("writer.serialize(").append(readValue);
				} else {
					code.append("writer.serializeRaw(").append(readValue);
				}
				if (attr.isMap) {
					code.append(", key_writer_").append(attr.name).append(context.useLazyResolution(types.get(0)) ? "()" : "");
					code.append(", value_writer_").append(attr.name).append(context.useLazyResolution(types.get(1)) ? "()" : "").append(");\n");
				} else {
					String content = extractSingleType(attr, types);
					code.append(", writer_").append(attr.name).append(context.useLazyResolution(content) ? "()" : "").append(");\n");
				}
			} else if (attr.isGeneric && !attr.containsStructOwnerType) {
				String content = extractSingleType(attr, null);
				if (attr.isArray) {
					code.append("writer.serialize(").append(readValue).append(", writer_").append(attr.name);
					code.append(context.useLazyResolution(content) ? "()" : "").append(");\n");
				} else {
					code.append("writer_").append(attr.name).append(context.useLazyResolution(content) ? "()" : "").append(".write(writer, ").append(readValue).append(");\n");
				}
			} else {
				code.append("writer_").append(attr.name).append("().write(writer, ").append(readValue).append(");\n");
			}
		}
	}

	private void handleSwitch(StructInfo si, String alignment, boolean useInstance) throws IOException {
		for (AttributeInfo attr : si.attributes.values()) {
			if (!attr.canReadInput()) continue;
			String name = si.propertyName(attr);
			code.append(alignment).append("\tcase ").append(Integer.toString(StructInfo.calcHash(name))).append(":\n");
			for (String an : attr.alternativeNames) {
				code.append(alignment).append("\tcase ").append(Integer.toString(StructInfo.calcHash(an))).append(":\n");
			}
			if (attr.fullMatch) {
				code.append(alignment).append("\t\tif (!reader.wasLastName(name_").append(attr.name).append(")) {\n");
				if (si.onUnknown == CompiledJson.Behavior.FAIL) {
					code.append(alignment).append("\t\tthrow reader.newParseErrorWith(\"Unknown property detected\", reader.getLastName().length() + 3, \"\", \"Unknown property detected\", reader.getLastName(), \"\");\n");
				} else {
					code.append(alignment).append("\t\treader.getNextToken(); reader.skip(); break;\n");
				}
				code.append(alignment).append("\t\t}\n");
			}
			boolean nonPrimitive = attr.typeName.equals(Analysis.objectName(attr.typeName));
			String defaultValue = context.getDefault(attr);
			if (attr.mandatory || attr.notNull && nonPrimitive && (useInstance || "null".equals(defaultValue) || context.isObjectInstance(attr))) {
				code.append(alignment).append("\t\t__detected_").append(attr.name).append("__ = true;\n");
			}
			code.append(alignment).append("\t\treader.getNextToken();\n");
			processPropertyValue(attr, alignment, useInstance, si.genericSignatures);
			code.append(alignment).append("\t\treader.getNextToken();\n");
			code.append(alignment).append("\t\tbreak;\n");
		}
		if (si.discriminator.length() > 0 && !si.attributes.containsKey(si.discriminator)) {
			code.append(alignment).append("\tcase ").append(Integer.toString(StructInfo.calcHash(si.discriminator))).append(":\n");
			String name = si.deserializeName.isEmpty() ? si.binaryName.replace('$', '.') : si.deserializeName;
			if (si.onUnknown == CompiledJson.Behavior.FAIL) {
				code.append(alignment).append("\t\treader.getNextToken();\n");
				code.append(alignment).append("\t\treader.calcHash();\n");
				code.append(alignment).append("\t\tif (!reader.wasLastName(\"").append(name).append("\")) {\n");
				code.append(alignment).append("\t\t\tthrow reader.newParseErrorWith(\"Unknown property detected\", reader.getLastName().length() + 3, \"\", \"Unknown property detected\", reader.getLastName(), \"\");\n");
				code.append(alignment).append("\t\t}\n");
				code.append(alignment).append("\t\treader.getNextToken();\n");
			} else {
				code.append(alignment).append("\t\treader.skip();\n");
			}
			code.append(alignment).append("\t\tbreak;\n");
		}
		code.append(alignment).append("\tdefault:\n");
		if (si.onUnknown == CompiledJson.Behavior.FAIL) {
			code.append(alignment).append("\t\tString lastName = reader.getLastName();\n");
			code.append(alignment).append("\t\tthrow reader.newParseErrorWith(\"Unknown property detected\", lastName.length() + 3, \"\", \"Unknown property detected\", lastName, \"\");\n");
		} else {
			code.append(alignment).append("\t\treader.getNextToken();\n");
			code.append(alignment).append("\t\treader.skip();\n");
		}
	}

	private void processPropertyValue(
			AttributeInfo attr,
			String alignment,
			boolean useInstance,
			Map<String, TypeMirror> genericSignatures) throws IOException {
		if (attr.notNull) {
			code.append(alignment).append("\t\tif (reader.wasNull()) throw reader.newParseErrorAt(\"Property '").append(attr.name).append("' is not allowed to be null\", 0);\n");
		}
		OptimizedConverter optimizedConverter = context.inlinedConverters.get(attr.typeName);
		String assignmentEnding = useInstance && attr.field == null ? ");\n" : ";\n";
		StructInfo target = context.structs.get(attr.typeName);
		if (attr.isJsonObject && attr.converter == null && target != null) {
			if (!attr.notNull) {
				code.append(alignment).append("\t\tif (reader.wasNull()) ");
				if (useInstance) {
					code.append("instance.");
					if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = null;\n");
					else if (attr.writeMethod != null) code.append(attr.writeMethod.getSimpleName()).append("(null);\n");
					else throw new RuntimeException("Unexpected code path in JsonObject. Please report this bug");
				} else {
					code.append("_").append(attr.name).append("_ = null;\n");
				}
			}
			code.append(alignment).append("\t\telse if (reader.last() == '{') {\n");
			code.append(alignment).append("\t\t\treader.getNextToken();\n");
			if (useInstance) {
				code.append(alignment).append("\t\t\tinstance.");
				if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
				else if (attr.writeMethod != null) code.append(attr.writeMethod.getSimpleName()).append("(");
				else throw new RuntimeException("Unexpected code path in JsonObject. Please report this bug");
				code.append(attr.typeName).append(".").append(target.jsonObjectReaderPath).append(".deserialize(reader)").append(assignmentEnding);
			} else {
				code.append(alignment).append("\t\t\t_").append(attr.name).append("_ = ").append(attr.typeName);
				code.append(".").append(target.jsonObjectReaderPath).append(".deserialize(reader);\n");
			}
			code.append(alignment).append("\t\t} else throw reader.newParseError(\"Expecting '{' as start for '").append(attr.name).append("'\");\n");
		} else if ((target == null || target.converter == null) && attr.converter == null && optimizedConverter != null && optimizedConverter.defaultValue == null && !attr.notNull && optimizedConverter.hasNonNullableMethod()) {
			if (useInstance) {
				if (attr.field == null && attr.writeMethod == null) {
					throw new RuntimeException("Unexpected code path for optimized converter. Please report this bug");
				}
				code.append(alignment).append("\t\tif (reader.wasNull()) instance.");
				if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = null;\n");
				else code.append(attr.writeMethod.getSimpleName()).append("(null);\n");
				code.append(alignment).append("\t\telse instance.");
				if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
				else code.append(attr.writeMethod.getSimpleName()).append("(");
			} else {
				code.append(alignment).append("\t\t_").append(attr.name).append("_ = reader.wasNull() ? null : ");
			}
			code.append(optimizedConverter.nonNullableDecoder()).append("(reader)").append(assignmentEnding);
		} else {
			if (useInstance) {
				code.append(alignment).append("\t\tinstance.");
				if (attr.field != null) code.append(attr.field.getSimpleName()).append(" = ");
				else if (attr.writeMethod != null) code.append(attr.writeMethod.getSimpleName()).append("(");
				else if (attr.readMethod != null && (attr.isList || attr.isSet)) code.append(attr.readMethod.getSimpleName()).append("().addAll(");
				else throw new RuntimeException("Unexpected code path. Expecting to setup a value. Please report this bug");
			} else {
				code.append(alignment).append("\t\t_").append(attr.name).append("_ = ");
			}
			List<String> types = attr.collectionContent(context.typeSupport, context.structs);
			if (attr.converter != null || target != null && target.converter != null) {
				ConverterInfo converter = attr.converter != null ? attr.converter : target.converter;
				if (useInstance && !converter.binder.isEmpty()) {
					converter.bind(code);
					code.append(".bind(reader, instance.");
					if (attr.field != null) code.append(attr.field.getSimpleName());
					else if (attr.readMethod != null) code.append(attr.readMethod.getSimpleName()).append("()");
					else throw new RuntimeException("Unexpected code path. Expecting to bind a value. Please report this bug");
					code.append(")");
				} else {
					converter.read(code);
					code.append(".read(reader)");
				}
			} else if (optimizedConverter != null) {
				boolean isPrimitive = !attr.typeName.equals(Analysis.objectName(attr.typeName));
				if (attr.notNull || isPrimitive) {
					code.append(optimizedConverter.nonNullableDecoder()).append("(reader)");
				} else {
					code.append(optimizedConverter.decoderField).append(".read(reader)");
				}
			} else if (target != null && attr.isEnum(context.structs)) {
				if (!attr.notNull) code.append("reader.wasNull() ? null : ");
				if (enumTemplate.isStatic(target)) {
					code.append(findConverterName(target)).append(".EnumConverter.readStatic(reader)");
				} else {
					code.append("converter_").append(attr.name).append(".read(reader)");
				}
			} else if (types != null) {
				context.serializeKnownCollection(attr, types, genericSignatures);
			} else if (attr.isGeneric && !attr.containsStructOwnerType) {
				if (attr.isArray) {
					String content = context.extractRawType(((ArrayType) attr.type).getComponentType(), genericSignatures);
					code.append("(").append(content).append("[])reader.readArray(reader_").append(attr.name);
					code.append(", emptyArray_").append(attr.name).append(")");
				} else {
					code.append("reader_").append(attr.name).append(".read(reader)");
				}
			} else {
				code.append("reader_").append(attr.name).append("().read(reader)");
			}
			code.append(assignmentEnding);
		}
	}

	private static int calcWeakHash(String name) {
		int hash = 0;
		for (int i = 0; i < name.length(); i++) {
			hash += (byte) name.charAt(i);
		}
		return hash;
	}
}
