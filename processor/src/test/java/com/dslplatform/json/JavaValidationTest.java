package com.dslplatform.json;

import com.dslplatform.json.models.*;
import com.dslplatform.json.processor.CompiledJsonAnnotationProcessor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.*;

public class JavaValidationTest extends AbstractAnnotationProcessorTest {

	protected Collection<Processor> getProcessors() {
		return Collections.<Processor>singletonList(new CompiledJsonAnnotationProcessor());
	}
	protected List<String> getDefaultArguments() {
		return Collections.emptyList();
	}

	@Test
	public void testEmptyValidClass() {
		checkValidCompilation(ValidCtor.class);
	}

	//TODO: immutable support
	@Ignore
	@Test
	public void testMissingEmptyCtor() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(MissingEmptyCtor.class),
				"'com.dslplatform.json.models.MissingEmptyCtor' requires public no argument constructor");
	}

	@Test
	public void testNonPublicClass() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				3,
				compileTestCase(NonPublicClass.class),
				"therefore 'com.dslplatform.json.NonPublicClass' must be public");
	}

	@Test
	public void testValidPropertyType() {
		checkValidCompilation(ValidType.class);
	}

	//TODO: char is supported
	@Ignore
	@Test
	public void testUnsupportedPropertyType() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				9,
				compileTestCase(InvalidType.class),
				"Specified type is not supported: 'char'");
	}

	@Test
	public void testReferencePropertyType() {
		checkValidCompilation(ReferenceType.class);
	}

	@Test
	public void testReferenceListPropertyType() {
		checkValidCompilation(ReferenceListType.class);
	}

	@Test
	public void testEnum() {
		checkValidCompilation(SimpleEnum.class);
	}

	@Test
	public void testEnumWithCtor() {
		checkValidCompilation(EnumWithArgs.class);
	}

	@Test
	public void testNestedNonStaticClass() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				6,
				compileTestCase(NestedNonStaticClass.class),
				"'com.dslplatform.json.models.NestedNonStaticClass.NonStaticClass' can't be a nested member. Only static nested classes are supported");
	}

	@Test
	public void testNestedStaticClass() {
		checkValidCompilation(NestedStaticClass.class);
	}

	@Test
	public void canIgnoreUnsupportedProperty() {
		checkValidCompilation(IgnoredProperty.class);
	}

	@Test
	public void coverAllTypes() {
		checkValidCompilation(AllTypes.class);
	}

	@Test
	public void checkIgnore() {
		checkValidCompilation(IgnoredProperty.class);
	}

	@Test
	public void checkAlias() {
		checkValidCompilation(PropertyAlias.class);
	}

	@Test
	public void checkNonNull() {
		checkValidCompilation(NonNullableReferenceProperty.class);
	}

	@Test
	public void correctCasing() {
		checkValidCompilation(ValidType.class);
	}

	@Test
	public void fieldsAreRecognized() {
		checkValidCompilation(ValidType.class);
	}

	@Test
	public void duplicateAlias() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				10,
				compileTestCase(DuplicatePropertyAlias.class),
				"Duplicate alias detected on field: prop");
	}

	@Test
	public void checkMinifiedNames() {
		checkValidCompilation(MinifiedProperties.class);
	}

	@Test
	public void supportsInterfaces() {
		checkValidCompilation(UsesInterfaceType.class, Implements1Type.class);
	}

	//TODO: mixins
	@Ignore
	@Test
	public void missingImplementations() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				7,
				compileTestCase(UsesInterfaceType.class),
				"Property iface is referencing interface (com.dslplatform.json.models.InterfaceType) which doesn't have registered implementations with @CompiledJson. At least one implementation of specified interface must be annotated with CompiledJson annotation");
	}

	@Test
	public void supportsAbstractClasses() {
		checkValidCompilation(UsesAbstractType.class, ExtendsType.class);
	}

	@Test
	public void supportsAbstractClassesWithConfiguration() {
		checkValidCompilation(UsesAbstractTypeWithConfiguration.class, ExtendsType.class, ExtendsTypeWithConfiguration.class);
	}

	@Test
	public void supportsInterfacesWithConfiguration() {
		checkValidCompilation(UsesInterfaceWithConfiguration.class, Implements1Type.class, InterfaceTypeWithoutSignature.class);
	}

	@Test
	public void willReadJsonAttributeOfClass() {
		checkValidCompilation(InterfaceTypeWithoutSignature.class);
	}

	@Test
	public void nestedAbstractMustBeStatic() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(AbstractTypeWithNoStaticConcrete.class),
				"Only public static nested classes are supported");
	}

	@Test
	public void deserializeAsMustBeRelated() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(InterfaceIntoNonRelated.class),
				"but specified deserializeAs target: 'com.dslplatform.json.models.InterfaceIntoNonRelated.Concrete' is not assignable to 'com.dslplatform.json.models.InterfaceIntoNonRelated'");
	}

	@Test
	public void onlyAbstractCanBeUsedWithDeserializeAs() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(InvalidConcreteWithDeserializeAs.class),
				"but specified deserializeAs target: 'com.dslplatform.json.models.InvalidConcreteWithDeserializeAs.Something' can only be specified for interfaces and abstract classes. 'com.dslplatform.json.models.InvalidConcreteWithDeserializeAs' is neither interface nor abstract class");
	}

	@Test
	public void selfDeserializeAs() {
		checkValidCompilation(DeserializeAsSelf.class);
	}

	@Test
	public void mustTargetConcreteTypeOnDeserializeAs() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(InterfaceIntoInterface.class),
				"deserializeAs target: 'com.dslplatform.json.models.InterfaceIntoInterface.Iface' must be a concrete type");
	}

	@Test
	public void deserializeAsCheck() {
		checkValidCompilation(AbstractTypeIntoConcreteType.class);
	}

	@Test
	public void checkInheritance() {
		checkValidCompilation(ExtendsType.class);
	}

	@Test
	public void noOuputByDefault() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(new ArrayList<String>(), ValidType.class);
		Assert.assertEquals(0, diagnostics.size());
	}

	@Test
	public void checkImplicitReference() {
		checkValidCompilation(ReferenceToImplicitType.class, ImplicitType.class);
	}

	@Test
	public void checkNonJavaImplicitReference() {
		assertCompilationSuccessful(
				compileTestCase(
						Collections.singletonList("-Adsljson.annotation=NON_JAVA"),
						ReferenceToImplicitType.class, ImplicitType.class));
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
   		Assert.assertEquals(2, diagnostics.size());
		Diagnostic note = diagnostics.get(0);
		Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		String error = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(error.contains("Annotation usage is set to non-java, but 'java.util.GregorianCalendar' is found in java package"));
		Assert.assertTrue(error.contains("java.util.GregorianCalendar is referenced as field from 'com.dslplatform.json.models.ImplicitWithJavaType'"));
	}

	@Test
	public void jsonObjectReferences() {
		checkValidCompilation(ReferenceJsonObject.class);
	}

	@Test
	public void hashMatchAnnotation() {
		checkValidCompilation(SerializationMatch.class);
	}

	@Test
	public void validClassConverter() {
		checkValidCompilation(CalendarPojo.class);
	}

	@Test
	public void invalidConverterErrors() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(InvalidConveterErrors.class);
		Assert.assertEquals(3, diagnostics.size());
		for (Diagnostic note : diagnostics) {
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
		checkValidCompilation(DuplicateHashAllowed.class);
	}

	@Test
	public void allowedDuplicatesOnSameProperty() {
		checkValidCompilation(DuplicateAlternativeHashAllowed.class);
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
		assertCompilationSuccessful(compileTestCase(DecimalPropertyConverter.class));
	}

	@Test
	public void invalidPropertyConverter() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				13,
				compileTestCase(InvalidDecimalPropertyConverter.class),
				"Specified converter: 'com.dslplatform.json.models.InvalidDecimalPropertyConverter.FormatDecimal2' has invalid type for JSON_READER field. It must be of type: 'com.dslplatform.json.JsonReader.ReadObject<java.math.BigDecimal>'");
	}

	@Test
	public void validPrimitivePropertyConverter() {
		checkValidCompilation(PrimitivePropertyConverter.class);
	}

	@Test
	public void validCustomArrayConverter() {
		checkValidCompilation(CustomArrayConverter.class);
	}

	@Test
	public void mandatoryProperties() {
		checkValidCompilation(RequiredProperty.class);
	}

	@Test
	public void failUnknownCheck() {
		checkValidCompilation(PropertyAlias.class);
	}

	@Test
	public void onUnknownDefault() {
		checkValidCompilation(ValidCtor.class);
	}
}
