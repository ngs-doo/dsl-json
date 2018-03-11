package com.dslplatform.json.processor;

import com.dslplatform.json.CompiledJson;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class StructInfo {
	public final TypeElement element;
	public final String name;
	public final ObjectType type;
	public final String converter;
	public final ExecutableElement constructor;
	public final Set<StructInfo> implementations = new HashSet<StructInfo>();
	public final Map<String, String> minifiedNames = new HashMap<String, String>();
	public final AnnotationMirror annotation;
	public final CompiledJson.Behavior onUnknown;
	public final CompiledJson.TypeSignature typeSignature;
	public final TypeElement deserializeAs;
	public final boolean isMinified;
	public final LinkedHashSet<CompiledJson.Format> formats;
	public final LinkedHashMap<String, AttributeInfo> attributes = new LinkedHashMap<String, AttributeInfo>();
	public final Set<Element> properties = new HashSet<Element>();
	public final List<String> constants = new ArrayList<String>();
	public final Stack<String> path = new Stack<String>();
	public final Map<String, TypeMirror> unknowns = new LinkedHashMap<String, TypeMirror>();

	public StructInfo(
			TypeElement element,
			String name,
			ObjectType type,
			boolean isJsonObject,
			ExecutableElement constructor,
			AnnotationMirror annotation,
			CompiledJson.Behavior onUnknown,
			CompiledJson.TypeSignature typeSignature,
			TypeElement deserializeAs,
			boolean isMinified,
			CompiledJson.Format[] formats) {
		this.element = element;
		this.name = name;
		this.type = type;
		this.converter = isJsonObject ? "" : null;
		this.constructor = constructor;
		this.annotation = annotation;
		this.onUnknown = onUnknown;
		this.typeSignature = typeSignature;
		this.deserializeAs = deserializeAs;
		this.isMinified = isMinified;
		this.formats = new LinkedHashSet<CompiledJson.Format>(formats == null ? Collections.singletonList(CompiledJson.Format.OBJECT) : Arrays.asList(formats));
	}

	public StructInfo(TypeElement converter, TypeElement target, String name) {
		this.element = target;
		this.name = name;
		this.type = ObjectType.CONVERTER;
		this.converter = converter.getQualifiedName().toString();
		this.constructor = null;
		this.annotation = null;
		this.onUnknown = null;
		this.typeSignature = null;
		this.deserializeAs = null;
		this.isMinified = false;
		this.formats = new LinkedHashSet<CompiledJson.Format>(Collections.singletonList(CompiledJson.Format.OBJECT));
	}

	public boolean hasEmptyCtor() {
		return constructor != null && constructor.getParameters().size() == 0;
	}

	public boolean hasAnnotation() {
		return annotation != null;
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

	private StructInfo deserializeTarget;
	public StructInfo deserializeTarget() { return deserializeTarget; }
	public void deserializeTarget(StructInfo value) { deserializeTarget = value; }

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
