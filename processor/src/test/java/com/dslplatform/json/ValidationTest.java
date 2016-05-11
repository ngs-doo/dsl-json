package com.dslplatform.json;

import com.dslplatform.json.models.*;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.*;

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
	public void testNonPublicClass() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 3, compileTestCase(NonPublicClass.class));
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
	public void testEnumWithCtor() {
		assertCompilationSuccessful(compileTestCase(EnumWithArgs.class));
	}

	@Test
	public void testNestedNonStaticClass() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 6, compileTestCase(NestedNonStaticClass.class));
	}

	@Test
	public void testNestedStaticClass() {
		assertCompilationSuccessful(compileTestCase(NestedStaticClass.class));
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
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertFalse(dsl.contains(" prop"));
		Assert.assertFalse(dsl.contains(" field"));
		Assert.assertFalse(dsl.contains("string? name"));
	}

	@Test
	public void checkAlias() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(PropertyAlias.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("int num {  serialization name 'y';  }"));
		Assert.assertTrue(dsl.contains("string? prop {  serialization name 'x';  }"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.PropertyAlias';"));
	}

	@Test
	public void checkNonNull() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(NonNullableReferenceProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string prop;"));
		Assert.assertTrue(dsl.contains("Set<uuid?> uuid;"));
		Assert.assertTrue(dsl.contains("json.struct0?[] ref;") || dsl.contains("json.struct1?[] ref;") || dsl.contains("json.struct2?[] ref;"));
		Assert.assertTrue(dsl.contains("json.struct0 enum;") || dsl.contains("json.struct1 enum;") || dsl.contains("json.struct2 enum;"));
	}

	@Test
	public void correctCasing() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ValidType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("int u;"));
		Assert.assertTrue(dsl.contains("int URI;"));
	}

	@Test
	public void fieldsAreRecognized() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ValidType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string? simpleField {  simple Java access;"));
		Assert.assertTrue(dsl.contains("List<string?>? listField {  simple Java access;"));
	}

	@Test
	public void duplicateAlias() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 10, compileTestCase(DuplicatePropertyAlias.class));
	}

	@Test
	public void checkMinifiedNames() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(MinifiedProperties.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("int width {  simple Java access;  serialization name 'w';  }"));
		Assert.assertTrue(dsl.contains("int height {  simple Java access;  serialization name 'h';  }"));
		Assert.assertTrue(dsl.contains("string? name {  simple Java access;  serialization name 'n0';  }"));
		Assert.assertTrue(dsl.contains("int customNumber {  simple Java access;  serialization name 'n';  }"));
	}

	@Test
	public void supportsInterfaces() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(UsesInterfaceType.class, Implements1Type.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("with mixin"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.InterfaceType';"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.Implements1Type';"));
	}

	@Test
	public void missingImplementations() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 7, compileTestCase(UsesInterfaceType.class));
	}

	@Test
	public void supportsAbstractClasses() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(UsesAbstractType.class, ExtendsType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("with mixin"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.AbstractType';"));
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.ExtendsType';"));
		Assert.assertTrue(dsl.contains("long y {  simple Java access;"));
	}

	@Test
	public void checkInheritance() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ExtendsType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string? x {"));
		Assert.assertTrue(dsl.contains("long y {"));
		Assert.assertTrue(dsl.contains("int o;"));
	}

	@Test
	public void noOuputByDefault() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(new ArrayList<String>(), ValidType.class);
		Assert.assertEquals(0, diagnostics.size());
	}

	@Test
	public void checkImplicitReference() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ReferenceToImplicitType.class, ImplicitType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.ImplicitType';"));
		Assert.assertTrue(dsl.contains("? prop;"));
	}

	@Test
	public void checkNonJavaImplicitReference() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics =
				compileTestCase(
						Arrays.asList("-Adsljson.annotation=NON_JAVA", "-Adsljson.showdsl=true"),
						ReferenceToImplicitType.class, ImplicitType.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("external name Java 'com.dslplatform.json.models.ImplicitType';"));
		Assert.assertTrue(dsl.contains("? prop;"));
	}

	@Test
	public void checkExplicitReference() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics =
				compileTestCase(
						Collections.singletonList("-Adsljson.annotation=EXPLICIT"),
						ReferenceToImplicitType.class, ImplicitType.class);
		Assert.assertEquals(1, diagnostics.size());
		Diagnostic note = diagnostics.get(0);
		Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		String error = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(error.contains("Annotation usage is set to explicit, but 'com.dslplatform.json.models.ImplicitType' is used implicitly through references"));
	}

	@Test
	public void checkJavaAndImplicitReference() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics =
				compileTestCase(
						Collections.singletonList("-Adsljson.annotation=NON_JAVA"),
						ReferenceToImplicitWithJavaType.class, ImplicitWithJavaType.class);
		Assert.assertEquals(1, diagnostics.size());
		Diagnostic note = diagnostics.get(0);
		Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		String error = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(error.contains("Annotation usage is set to non-java, but 'java.util.Date' is found in java package"));
		Assert.assertTrue(error.contains("java.util.Date is referenced as field from 'com.dslplatform.json.models.ImplicitWithJavaType'"));
	}
}
