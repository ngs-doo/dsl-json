package com.dslplatform.json;

import com.dslplatform.json.models.*;
import com.dslplatform.json.processor.CompiledJsonAnnotationProcessor;
import org.junit.Assert;
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

	@Test
	public void testInvalidCtor() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(MissingEmptyCtor.class),
				"'com.dslplatform.json.models.MissingEmptyCtor' does not have an empty or matching constructor");
	}

	@Test
	public void testValidCtor() {
		checkValidCompilation(ImmutableClass.class);
	}

	@Test
	public void testCtorNameMismatch() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				12,
				compileTestCase(ImmutableClassMismatch.class),
				"Unable to find matching property: 'x' used in constructor");
	}

	@Test
	public void testNonPublicClass() {
		checkValidCompilation(NonPublicClass.class);
	}

	@Test
	public void canUseAnnotationOnStaticMethod() {
		checkValidCompilation(OnStaticMethod.class);
	}

	@Test
	public void cantUseAnnotationOnNonstaticMethod() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				19,
				compileTestCase(OnNonStaticMethod.class),
				"Factory method in 'com.dslplatform.json.models.OnNonStaticMethod' is annotated with com.dslplatform.json.CompiledJson, but it's not accessible.");
	}

	@Test
	public void testNestedFactory() {
		checkValidCompilation(NestedFactoryType.class, NestedFactoryType.Factory.class);
	}

	@Test
	public void testCompanionFactory() {
		checkValidCompilation(CompanionFactory.class, CompanionFactory.Companion.class);
	}

	@Test
	public void testValidPropertyType() {
		checkValidCompilation(ValidType.class);
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
	public void testEnumWithJsonValueAnnotationPlacedOnStringField() {
		checkValidCompilation(EnumWithCustomConstantName1.class);
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnStringGetter() {
		checkValidCompilation(EnumWithCustomConstantName2.class);
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnIntField() {
		checkValidCompilation(EnumWithCustomConstantName3.class);
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnIntGetter() {
		checkValidCompilation(EnumWithCustomConstantName4.class);
	}

	@Test
	public void testEnumWithJsonValueObjectAndConverter() {
		checkValidCompilation(EnumWithObjectAndConverter.class);
	}

	@Test
	public void testEnumWithJsonValueObjectAndNoConverter() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				18,
				compileTestCase(EnumWithObjectNoConverter.class),
				"Method 'getValue()' annotated with @JsonValue must be of a supported type.");
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnNonPublicFieldOrGetter() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				18,
				compileTestCase(EnumWithInvalidCustomConstantName1.class),
				"Method 'getStr()' annotated with @JsonValue must be public.");
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnUnsupportedFieldType() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				12,
				compileTestCase(EnumWithInvalidCustomConstantName2.class),
				"Field 'str' annotated with @JsonValue must be of a supported type. Unknown types");
	}

	@Test
	public void testEnumWithDuplicatedJsonValueAnnotations() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				19,
				compileTestCase(EnumWithInvalidCustomConstantName3.class),
				"Duplicate @JsonValue annotation found. Only one enum field or getter can be annotated.");
	}

	@Test
	public void testEnumWithJsonValueAnnotationPlacedOnEnumConstant() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				8,
				compileTestCase(EnumWithInvalidCustomConstantName4.class),
				"Unexpected @JsonValue annotation found. It must be placed on enum field or getter.");
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

	@Test
	public void missingImplementations() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(UsesInterfaceType.class);
		Assert.assertEquals(2, diagnostics.size());
		Assert.assertTrue(diagnostics.get(0).getMessage(Locale.ENGLISH).contains("Property iface is referencing interface (com.dslplatform.json.models.InterfaceType) which doesn't have registered implementations with @CompiledJson. At least one implementation of specified interface must be annotated with CompiledJson annotation"));
		Assert.assertEquals(7, diagnostics.get(0).getLineNumber());
		Assert.assertTrue(diagnostics.get(1).getMessage(Locale.ENGLISH).contains("Interface (com.dslplatform.json.models.InterfaceType) is referenced, but it doesn't have registered implementations with @CompiledJson. At least one implementation of specified Interface must be annotated with CompiledJson annotation"));
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
		Assert.assertEquals(4, diagnostics.size());
		Diagnostic note = diagnostics.get(0);
		Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		String error = note.getMessage(Locale.ENGLISH);
		Assert.assertTrue(error.contains("Annotation usage is set to non-java, but 'java.util.GregorianCalendar' is found in java package"));
		Assert.assertTrue(error.contains("java.util.GregorianCalendar is referenced as field from 'com.dslplatform.json.models.ImplicitWithJavaType'"));
	}

	@Test
	public void invalidJsonObjectReferences() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(ReferenceJsonObject.class);
		Assert.assertEquals(3, diagnostics.size());
		for (Diagnostic note : diagnostics) {
			Assert.assertEquals(Diagnostic.Kind.ERROR, note.getKind());
		}
		String error1 = diagnostics.get(0).getMessage(Locale.ENGLISH);
		String error2 = diagnostics.get(1).getMessage(Locale.ENGLISH);
		String error3 = diagnostics.get(2).getMessage(Locale.ENGLISH);
		Assert.assertTrue(error1.contains("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3' is 'com.dslplatform.json.JsonObject', but its JSON_READER field is not of correct type. It can't be used for serialization/deserialization this way. You probably want to change JSON_READER field to: 'com.dslplatform.json.JsonReader.ReadJsonObject<com.dslplatform.json.models.ReferenceJsonObject.ImplFailed3>'"));
		Assert.assertTrue(error2.contains("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed2' is 'com.dslplatform.json.JsonObject', but its JSON_READER field is not public and static. It can't be used for serialization/deserialization this way. You probably want to change JSON_READER field so it's public and static."));
		Assert.assertTrue(error3.contains("'com.dslplatform.json.models.ReferenceJsonObject.ImplFailed1' is 'com.dslplatform.json.JsonObject', but it doesn't have JSON_READER field/method. It can't be used for serialization/deserialization this way. You probably want to add public static JSON_READER field/method."));
	}

	@Test
	public void validJsonObjectReferences() {
		checkValidCompilation(ValidReferenceJsonObject.class);
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
		Assert.assertTrue(error1.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.CharConverter' doesn't have a JSON_READER or JSON_WRITER field/method"));
		Assert.assertTrue(error2.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.DateConverter' doesn't have public and static JSON_READER and JSON_WRITER field/method"));
		Assert.assertTrue(error3.contains("Specified converter: 'com.dslplatform.json.models.InvalidConveterErrors.ShortConverter' has invalid type for JSON_WRITER field/method"));
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

	@Test
	public void arrayFormatRequiresIndexOnProperties() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				8,
				compileTestCase(ArrayFormatWithoutIndex.class),
				"When array format is used all properties must have index order defined. Property o doesn't have index defined");
	}

	@Test
	public void arrayFormatAndDuplicateIndex() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				8,
				compileTestCase(ArrayFormatDuplicateIndex.class),
				"Duplicate index detected on y. Index values must be distinct to be used in array format");
	}

	@Test
	public void supportsArrayFormat() {
		checkValidCompilation(ArrayFormat.class);
	}
	@Test
	public void duplicateFormatCheck() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				5,
				compileTestCase(DuplicateFormat.class),
				"Duplicate format detected on 'com.dslplatform.json.models.DuplicateFormat'");
	}

	@Test
	public void deserializationNameWorksWithoutInterface() {
		checkValidCompilation(DeserializationName.class);
	}

	@Test
	public void deserializationNameWorksWithInterface() {
		checkValidCompilation(DeserializationNameWithInterface.class, DeserializationInterface.class);
	}

	@Test
	public void multipleCtors() {
		checkValidCompilation(MultipleCtors.class);
	}

	@Test
	public void inversedCtorArguments() {
		checkValidCompilation(InversedArguments.class);
	}

	@Test
	public void arrayFormatAndCtors() {
		checkValidCompilation(MultipleCtorsAndArray.class);
	}

	@Test
	public void jacksonAnnotation() {
		assertCompilationSuccessful(
				compileTestCase(
						Collections.singletonList("-Adsljson.jackson=true"),
						JacksonCreator.class));
	}

	@Test
	public void immutableAndGetter() {
		checkValidCompilation(ImmutableClassWithGetter.class);
	}

	@Test
	public void immutableAndInheritance() {
		checkValidCompilation(ImmutableHierarchy.Father.class);
	}

	@Test
	public void customCtorName() {
		checkValidCompilation(CustomCtorPropertyName.class);
	}

	@Test
	public void simpleEnums() {
		checkValidCompilation(WithEnums.class);
	}

	@Test
	public void ctorIndexOrder() {
		checkValidCompilation(ImmutablePerson.class);
	}

	@Test
	public void jsonReaderMethods() {
		checkValidCompilation(JsonReaderMethods.class);
	}

	@Test
	public void ctorWithLooseSignature() {
		checkValidCompilation(CtorWithLooseSignature.class);
	}

	@Test
	public void kotlinInstancePattern() {
		checkValidCompilation(KotlinInstancePattern.class);
	}

	@Test
	public void objectTypeRequireUnknownSettings() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				7,
				compileTestCase(InvalidObjectProperty.class),
				"Property o is referencing unknown type: 'java.lang.Object'. Register");
	}

	@Test
	public void rawTypeRequireUnknownSettings() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = compileTestCase(InvalidRawTypeProperty.class);
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				10,
				diagnostics,
				"Property rawList is referencing raw type: 'java.util.List'. Specify type arguments, register " +
						"custom converter, mark property as ignored or enable unknown types");

		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				11,
				diagnostics,
				"Property rawGeneric is referencing raw type: 'com.dslplatform.json.models.InvalidRawTypeProperty.MyGeneric'. " +
						"Specify type arguments, register custom converter, mark property as ignored or enable unknown types");

		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				12,
				diagnostics,
				"Property mapWithRawGeneric is referencing type: 'java.util.Map<java.lang.String,com.dslplatform.json.models.InvalidRawTypeProperty.MyGeneric>' " +
						"which has a raw type part: 'com.dslplatform.json.models.InvalidRawTypeProperty.MyGeneric'. " +
						"Specify type arguments, register custom converter, mark property as ignored or enable unknown types");
	}

	@Test
	public void stringVariants() {
		checkValidCompilation(BuildersAndBuffers.class);
	}

	@Test
	public void whitelistingObject() {
		checkValidCompilation(UnknownTypeWithConverter.class, UnknownTypeWithConverter.Generic.class);
	}

	@Test
	public void builderWithoutCtor() {
		checkValidCompilation(ValidBuilderWithoutCtor.class);
	}

	@Test
	public void builderWithClassAnnotation() {
		checkValidCompilation(ValidBuilderAnnotationOnClass.class);
	}

	@Test
	public void invalidBuildWithBuildArg() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				27,
				compileTestCase(InvalidBuilderWithArgument.class, InvalidBuilderWithArgument.Builder.class),
				"Builder method: 'build' can't have parameters");
	}

	@Test
	public void builderWithAbstract() {
		checkValidCompilation(ValidAbstractBuilder.class);
	}

	@Test
	public void builderWithNested() {
		checkValidCompilation(FreeBuilder.class);
	}

	@Test
	public void invalidBuildWithCtorArgs() {
		assertCompilationReturned(
				Diagnostic.Kind.ERROR,
				15,
				compileTestCase(InvalidBuilderWithCtorArgs.class, InvalidBuilderWithCtorArgs.Builder.class),
				"Builder constructor for: 'com.dslplatform.json.models.InvalidBuilderWithCtorArgs.Builder' can't have parameters");
	}

	@Test
	public void genericWithTypes() {
		checkValidCompilation(GenericWithVariousTypes.class);
	}

	@Test
	public void annotationInCtor() {
		checkValidCompilation(MapRuntimeCtor.class);
	}

}
