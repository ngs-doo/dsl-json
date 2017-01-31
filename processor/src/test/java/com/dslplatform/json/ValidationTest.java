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
		Diagnostic diagnostic = assertCompilationReturned(Diagnostic.Kind.ERROR, 9, compileTestCase(InvalidType.class));
		Assert.assertTrue(diagnostic.getMessage(Locale.ENGLISH).contains("Specified type not supported: 'char'"));
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
		Assert.assertFalse(dsl.contains(" field1"));
		Assert.assertFalse(dsl.contains(" field2"));
		Assert.assertFalse(dsl.contains("string? name"));
	}

	@Test
	public void checkAlias() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(PropertyAlias.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("int num {  serialization name 'y';  }"));
		Assert.assertTrue(dsl.contains("string? prop {  serialization name 'x';  deserialization alias 'X';  deserialization alias 'old_prop';  }"));
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

	@Test
	public void jsonObjectReferences() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ReferenceJsonObject.class);
		String warning1 = diagnostics.get(diagnostics.size() - 4).getMessage(Locale.ENGLISH);
		String warning2 = diagnostics.get(diagnostics.size() - 3).getMessage(Locale.ENGLISH);
		String warning3 = diagnostics.get(diagnostics.size() - 2).getMessage(Locale.ENGLISH);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.replace("  ", "").contains("{\n" +
				"external Java JSON converter;\n" +
				"external name Java 'com.dslplatform.json.models.ReferenceJsonObject.ImplProper';\n" +
				"}"));
		Assert.assertTrue(dsl.replace("  ", "").contains("{\n" +
				"external name Java 'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed1';\n" +
				"}"));
		Assert.assertTrue(dsl.replace("  ", "").contains("{\n" +
				"external name Java 'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed2';\n" +
				"}"));
		Assert.assertTrue(dsl.replace("  ", "").contains("{\n" +
				"external name Java 'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3';\n" +
				"}"));
		Assert.assertTrue(
				warning1.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed1' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field.")
						|| warning2.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed1' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field.")
						|| warning3.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed1' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field.")
		);
		Assert.assertTrue(
				warning1.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed2' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not public and static.")
						|| warning2.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed2' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not public and static.")
						|| warning3.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed2' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not public and static.")
		);
		Assert.assertTrue(
				warning1.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not of correct type.")
						|| warning2.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not of correct type.")
						|| warning3.startsWith("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3' is 'com.dslplatform.json.JsonObject', but it's JSON_READER field is not of correct type.")
		);
	}

	@Test
	public void hashMatchAnnotation() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(SerializationMatch.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string? hash {  simple Java access;  }"));
		Assert.assertTrue(dsl.contains("string? full {  simple Java access;  deserialization match full;  }"));
		Assert.assertTrue(dsl.contains("string? def {  simple Java access;  }"));
	}

	@Test
	public void validClassConverter() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(DatePojo.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.replace("  ", "").contains("{\n" +
				"external Java JSON converter 'com.dslplatform.json.models.DatePojo.DateConverter';\n" +
				"external name Java 'java.util.Date';\n" +
				"}"));
	}

	@Test
	public void invalidConverterErrors() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(InvalidConveterErrors.class);
		Assert.assertEquals(9, diagnostics.size());
		for(Diagnostic note : diagnostics) {
			Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		}
		String error1 = diagnostics.get(0).getMessage(Locale.ENGLISH);
		String error2 = diagnostics.get(1).getMessage(Locale.ENGLISH);
		String error3 = diagnostics.get(2).getMessage(Locale.ENGLISH);
		Assert.assertTrue(error1.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.CharConverter' doesn't have a JSON_READER or JSON_WRITER field"));
		Assert.assertTrue(error2.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.DateConverter' doesn't have public and static JSON_READER and JSON_WRITER fields"));
		Assert.assertTrue(error3.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.ShortConverter' has invalid type for JSON_WRITER field"));
		Assert.assertTrue(error3.contains("must be of type: 'com.dslplatform.json.JsonWriter.WriteObject<java.lang.Short>'"));
	}

	@Test
	public void allowedDuplicatesInDifferentProperties() {
		assertCompilationSuccessful(compileTestCase(DuplicateHashAllowed.class));
	}

	@Test
	public void allowedDuplicatesOnSameProperty() {
		assertCompilationSuccessful(compileTestCase(DuplicateAlternativeHashAllowed.class));
	}

	@Test
	public void disallowDuplicatesOnDifferentAlternatives() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(DuplicateHashNotAllowed.class);
		Assert.assertEquals(1, diagnostics.size());
		Assert.assertEquals(Diagnostic.Kind.ERROR, diagnostics.get(0).getKind());
		String error = diagnostics.get(0).getMessage(Locale.ENGLISH);
		Assert.assertTrue(error.contains("Duplicate hash value detected"));
	}

	@Test
	public void validPropertyConverter() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(DecimalPropertyConverter.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(
				dsl.contains(
						"external Java JSON converter 'com.dslplatform.json.models.DecimalPropertyConverter.FormatDecimal2' for 'java.math.BigDecimal';"));
		Assert.assertFalse(dsl.contains("external name Java 'java.math.BigDecimal';"));
	}

	@Test
	public void invalidPropertyConverter() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 13, compileTestCase(InvalidDecimalPropertyConverter.class));
	}

	@Test
	public void validPrimitivePropertyConverter() {
		assertCompilationSuccessful(compileTestCase(PrimitivePropertyConverter.class));
	}

	@Test
	public void validCustomArrayConverter() {
		assertCompilationSuccessful(compileTestCase(CustomArrayConverter.class));
	}

	@Test
	public void mandatoryProperties() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(RequiredProperty.class);
		Diagnostic note = diagnostics.get(diagnostics.size() - 1);
		Assert.assertEquals(Diagnostic.Kind.NOTE, note.getKind());
		String dsl = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(dsl.contains("string? field1 {  simple Java access;  mandatory;  }"));
		Assert.assertTrue(dsl.contains("string? field2 {  simple Java access;  mandatory;  }"));
		Assert.assertTrue(dsl.contains("string? field3 {  simple Java access;  }"));
		Assert.assertTrue(dsl.contains("string? field4 {  simple Java access;  }"));
	}
}
