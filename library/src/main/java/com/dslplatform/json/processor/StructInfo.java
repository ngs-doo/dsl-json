package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class StructInfo {
	public final TypeElement element;
	public final DeclaredType discoveredBy;
	public final String name;
	public final String binaryName;
	public final ObjectType type;
	public final ConverterInfo converter;
	public final String jsonObjectReaderPath;
	public final List<ExecutableElement> matchingConstructors;
	public final ExecutableElement annotatedFactory;
	public final ExecutableElement annotatedConstructor;
	public final BuilderInfo builder;
	public final Set<StructInfo> implementations = new HashSet<StructInfo>();
	private StructInfo inheritsFrom;
	public final Map<String, String> minifiedNames = new HashMap<String, String>();
	public final AnnotationMirror annotation;
	public final CompiledJson.Behavior onUnknown;
	public final CompiledJson.TypeSignature typeSignature;
	public final CompiledJson.ObjectFormatPolicy objectFormatPolicy;
	public final TypeElement deserializeAs;
	public final String discriminator;
	public final String deserializeName;
	public final boolean isMinified;
	public final EnumSet<CompiledJson.Format> formats;
	public final boolean isObjectFormatFirst;
	public final LinkedHashMap<String, AttributeInfo> attributes = new LinkedHashMap<String, AttributeInfo>();
	public final Set<Element> properties = new HashSet<Element>();
	public final List<String> constants = new ArrayList<String>();
	public final Element enumConstantNameSource;
	public final Stack<String> path = new Stack<String>();
	public final Map<String, TypeMirror> unknowns = new LinkedHashMap<String, TypeMirror>();
	public final boolean isParameterized;
	public final List<String> typeParametersNames;
	public final Map<String, TypeMirror> genericSignatures;

	private ExecutableElement selectedConstructor;
	private boolean createThroughConstructor;

	public StructInfo(
			TypeElement element,
			DeclaredType discoveredBy,
			String name,
			String binaryName,
			ObjectType type,
			@Nullable String jsonObjectReaderPath,
			@Nullable List<ExecutableElement> matchingConstructors,
			@Nullable ExecutableElement annotatedConstructor,
			@Nullable ExecutableElement annotatedFactory,
			@Nullable BuilderInfo builder,
			@Nullable AnnotationMirror annotation,
			@Nullable CompiledJson.Behavior onUnknown,
			@Nullable CompiledJson.TypeSignature typeSignature,
			CompiledJson.ObjectFormatPolicy objectFormatPolicy,
			@Nullable TypeElement deserializeAs,
			@Nullable String discriminator,
			@Nullable String deserializeName,
			@Nullable Element enumConstantNameSource,
			boolean isMinified,
			@Nullable CompiledJson.Format[] formats,
			Map<String, TypeMirror> genericSignatures) {
		this.element = element;
		this.discoveredBy = discoveredBy;
		this.name = name;
		this.binaryName = binaryName;
		this.type = type;
		this.jsonObjectReaderPath = jsonObjectReaderPath;
		this.converter = null;
		this.matchingConstructors = matchingConstructors;
		this.annotatedFactory = annotatedFactory;
		this.builder = builder;
		this.annotation = annotation;
		this.onUnknown = onUnknown;
		this.typeSignature = typeSignature;
		this.objectFormatPolicy = objectFormatPolicy;
		this.deserializeAs = deserializeAs;
		this.discriminator = discriminator != null ? discriminator : "";
		this.deserializeName = deserializeName != null ? deserializeName : "";
		this.enumConstantNameSource = enumConstantNameSource;
		this.isMinified = isMinified;
		this.formats = formats == null ? EnumSet.of(CompiledJson.Format.OBJECT) : EnumSet.copyOf(Arrays.asList(formats));
		this.isObjectFormatFirst = formats == null || formats.length == 0 || formats[0] == CompiledJson.Format.OBJECT;
		this.createThroughConstructor = annotatedFactory == null && annotatedConstructor != null;
		if (annotatedConstructor != null) this.annotatedConstructor = this.selectedConstructor = annotatedConstructor;
		else if (matchingConstructors == null) this.annotatedConstructor = null;
		else if (matchingConstructors.size() == 1 && type == ObjectType.CLASS) {
			this.selectedConstructor = matchingConstructors.get(0);
			this.annotatedConstructor = null;
		} else {
			ExecutableElement emptyCtor = null;
			for (ExecutableElement ee : matchingConstructors) {
				if (ee.getParameters().size() == 0) {
					emptyCtor = ee;
					break;
				}
			}
			this.selectedConstructor = type == ObjectType.CLASS ? emptyCtor : null;
			this.annotatedConstructor = null;
		}
		this.typeParametersNames = extractParametersNames(element.getTypeParameters());
		this.isParameterized = !typeParametersNames.isEmpty();
		this.genericSignatures = genericSignatures;
	}

	public StructInfo(ConverterInfo converter, DeclaredType discoveredBy, TypeElement target, String name, String binaryName) {
		this.element = target;
		this.discoveredBy = discoveredBy;
		this.name = name;
		this.binaryName = binaryName;
		this.type = ObjectType.CONVERTER;
		this.jsonObjectReaderPath = null;
		this.converter = converter;
		this.matchingConstructors = null;
		this.annotatedConstructor = null;
		this.annotatedFactory = null;
		this.builder = null;
		this.annotation = null;
		this.onUnknown = null;
		this.typeSignature = null;
		this.objectFormatPolicy = CompiledJson.ObjectFormatPolicy.DEFAULT;
		this.deserializeAs = null;
		this.discriminator = "";
		this.deserializeName = "";
		this.enumConstantNameSource = null;
		this.isMinified = false;
		this.formats = EnumSet.of(CompiledJson.Format.OBJECT);
		this.isObjectFormatFirst = true;
		this.typeParametersNames = extractParametersNames(element.getTypeParameters());
		this.isParameterized = !typeParametersNames.isEmpty();
		this.genericSignatures = Collections.emptyMap();
	}

	private List<String> extractParametersNames(final List<? extends TypeParameterElement> typeParameters) {
		if (typeParameters.isEmpty()) return Collections.emptyList();
		List<String> names = new ArrayList<String>(typeParameters.size());
		for (TypeParameterElement typeParameter : typeParameters) {
			names.add(typeParameter.getSimpleName().toString());
		}
		return names;
	}

	@Nullable
	public ExecutableElement selectedConstructor() {
		return selectedConstructor;
	}

	public void supertype(@Nullable StructInfo parent) {
		if (parent == null) return;
		if (type == ObjectType.CLASS && parent.implementations.contains(this)) {
			inheritsFrom = parent;
		}
	}

	public Collection<AttributeInfo> inheritedAttributes() {
		if (inheritsFrom == null) return Collections.emptyList();
		return inheritsFrom.attributes.values();
	}

	public void useConstructor(ExecutableElement ctor) {
		if (matchingConstructors == null || !matchingConstructors.contains(ctor)) {
			throw new IllegalArgumentException("Specified ctor is not a part of matchingConstructors");
		}
		selectedConstructor = ctor;
		createThroughConstructor = true;
	}

	public boolean hasKnownConversion() {
		return jsonObjectReaderPath != null || converter != null;
	}

	public boolean usesEmptyCtor() {
		return (createThroughConstructor || annotatedFactory == null) && selectedConstructor != null && selectedConstructor.getParameters().size() == 0;
	}

	public boolean usesCtorWithArguments() {
		return (createThroughConstructor || annotatedFactory == null) && selectedConstructor != null && selectedConstructor.getParameters().size() > 0;
	}

	public boolean hasEmptyCtor() {
		if (matchingConstructors == null) return false;
		for (ExecutableElement ctor : matchingConstructors) {
			if (ctor.getParameters().isEmpty()) return true;
		}
		return false;
	}

	public boolean createFromEmptyInstance() {
		return annotatedFactory != null && annotatedFactory.getParameters().size() == 0
				|| annotatedFactory == null && selectedConstructor != null && selectedConstructor.getParameters().size() == 0;
	}

	public boolean hasAnnotation() {
		return annotation != null;
	}

	public boolean hasCycles(Map<String, StructInfo> structs) {
		return hasCycles(new HashSet<TypeMirror>(), structs);
	}

	private boolean hasCycles(HashSet<TypeMirror> processed, Map<String, StructInfo> structs) {
		if (type == ObjectType.ENUM || type == ObjectType.CONVERTER) return false;
		processed.add(element.asType());
		for (AttributeInfo ai : attributes.values()) {
			if (ai.converter != null || ai.isJsonObject) continue;
			if (ai.isGeneric) return true;
			for (TypeMirror tm : ai.usedTypes) {
				if (processed.add(tm)) {
					StructInfo find = structs.get(tm.toString());
					if (find != null && find.hasCycles(processed, structs)) return true;
				} else return true;
			}
		}
		return false;
	}

	public static int calcHash(String name) {
		long hash = 0x811c9dc5;
		for (int i = 0; i < name.length(); i++) {
			byte b = (byte) name.charAt(i);
			hash ^= b;
			hash *= 0x1000193;
		}
		return (int) hash;
	}

	public static int calcWeakHash(String name) {
		int hash = 0;
		for (int i = 0; i < name.length(); i++) {
			byte b = (byte) name.charAt(i);
			hash += b;
		}
		return hash;
	}

	private StructInfo deserializeTarget;
	@Nullable
	public StructInfo getDeserializeTarget() { return deserializeTarget; }
	public void setDeserializeTarget(@Nullable StructInfo value) { deserializeTarget = value; }

	public String pathDescription() {
		if (annotation != null) return " since it has @CompiledJson annotation.";
		if (path.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (String p : path) {
			sb.append(p).append("->");
		}
		sb.setLength(sb.length() - 2);
		sb.insert(0, " since it's referenced through path from @CompiledJson annotation: ");
		return sb.toString();
	}

	public boolean checkHashCollision() {
		boolean hasAliases = false;
		boolean hasDuplicates = false;
		Set<Integer> counters = new HashSet<Integer>();
		for (AttributeInfo attr : attributes.values()) {
			int hash = calcHash(attr.alias != null ? attr.alias : attr.name);
			hasDuplicates = hasDuplicates || !counters.add(hash);
			if (!attr.alternativeNames.isEmpty()) {
				hasAliases = true;
				for (String name : attr.alternativeNames) {
					int aliasHash = calcHash(name);
					if (aliasHash == hash) {
						continue;
					}
					hasDuplicates = hasDuplicates || !counters.add(aliasHash);
				}
			}
		}
		return hasAliases && hasDuplicates;
	}

	public void prepareMinifiedNames() {
		Map<Character, Integer> counters = new HashMap<Character, Integer>();
		Set<String> processed = new HashSet<String>();
		Set<String> names = new HashSet<String>();
		for (AttributeInfo p : attributes.values()) {
			if (p.alias != null) {
				minifiedNames.put(p.id, p.alias);
				processed.add(p.id);
				names.add(p.alias);
			}
		}
		for (AttributeInfo p : attributes.values()) {
			if (processed.contains(p.id)) {
				continue;
			}
			String shortName = buildShortName(p.id, names, counters);
			minifiedNames.put(p.id, shortName);
		}
	}

	public void sortAttributes() {
		boolean needsSorting = false;
		for (AttributeInfo attr : attributes.values()) {
			needsSorting = needsSorting || attr.index >= 0;
		}
		if (needsSorting) {
			final AttributeInfo[] all = attributes.values().toArray(new AttributeInfo[0]);
			Arrays.sort(all, new Comparator<AttributeInfo>() {
				@Override
				public int compare(AttributeInfo a, AttributeInfo b) {
					if (b.index == -1) return -1;
					else if (a.index == -1) return 1;
					return a.index - b.index;
				}
			});
			attributes.clear();
			for (AttributeInfo attr : all) {
				attributes.put(attr.id, attr);
			}
		}
	}

	private static String buildShortName(String name, Set<String> names, Map<Character, Integer> counters) {
		String shortName = name.substring(0, 1);
		Character first = name.charAt(0);
		if (!names.contains(shortName)) {
			names.add(shortName);
			counters.put(first, 0);
			return shortName;
		}
		Integer next = counters.get(first);
		if (next == null) {
			next = 0;
		}
		do {
			shortName = first.toString() + next;
			next++;
		} while (names.contains(shortName));
		counters.put(first, next);
		names.add(shortName);
		return shortName;
	}
}
