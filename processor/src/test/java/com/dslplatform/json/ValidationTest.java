package com.dslplatform.json;

import com.dslplatform.json.models.*;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ValidationTest extends AbstractAnnotationProcessorTest {

	protected Collection<Processor> getProcessors() {
		return Collections.<Processor>singletonList(new CompiledJsonProcessor());
	}

	@Test
	public void testEmptyValidClass() {
		assertCompilationSuccessful(compileTestCase(ValidCtor.class));
	}

	@Test
	public void testMissingEmptyCtor() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 5, compileTestCase(MissingEmptyCtor.class));
	}

	@Test
	public void testValidPropertyType() {
		assertCompilationSuccessful(compileTestCase(ValidType.class));
	}

	@Test
	public void testUnsupportedPropertyType() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 9, compileTestCase(InvalidType.class));
	}

	@Test
	public void testReferencePropertyType() {
		assertCompilationSuccessful(compileTestCase(ReferenceType.class));
	}

	@Test
	public void testReferenceListPropertyType() {
		assertCompilationSuccessful(compileTestCase(ReferenceListType.class));
	}

	@Test
	public void testEnum() {
		assertCompilationSuccessful(compileTestCase(SimpleEnum.class));
	}

	@Test
	public void canIgnoreUnsupportedProperty() {
		assertCompilationSuccessful(compileTestCase(IgnoredProperty.class));
	}

	@Test
	public void coverAllTypes() {
		assertCompilationSuccessful(compileTestCase(AllTypes.class));
	}

	@Test
	public void checkIgnore() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(IgnoredProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		Assert.assertEquals("Note: module json {\n  struct struct0 {\n"
				+ "    external name Java 'com.dslplatform.json.models.IgnoredProperty';\n  }\n}",
				note.getMessage(Locale.ENGLISH));
	}

	@Test
	public void checkAlias() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(PropertyAlias.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		Assert.assertEquals("Note: module json {\n  struct struct0 {\n"
						+ "    int num { serialization name 'y'; }\n"
						+ "    string? prop { serialization name 'x'; }\n"
						+ "    external name Java 'com.dslplatform.json.models.PropertyAlias';\n  }\n}",
				note.getMessage(Locale.ENGLISH));
	}

	@Test
	public void checkNonNull() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(NonNullableReferenceProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		Assert.assertEquals("Note: module json {\n"
						+ "  struct struct0 {\n"
						+ "    json.struct1?[] ref;\n"
						+ "    string prop;\n"
						+ "    json.struct2 enum;\n"
						+ "    Set<uuid?> uuid;\n"
						+ "    external name Java 'com.dslplatform.json.models.NonNullableReferenceProperty';\n"
						+ "  }\n"
						+ "  struct struct1 {\n"
						+ "    external name Java 'com.dslplatform.json.models.ValidCtor';\n"
						+ "  }\n"
						+ "  enum struct2 {\n"
						+ "    FIRST;\n"
						+ "    SECOND;\n"
						+ "    external name Java 'com.dslplatform.json.models.SimpleEnum';\n"
						+ "  }\n"
						+ "}",
				note.getMessage(Locale.ENGLISH));
	}
}
