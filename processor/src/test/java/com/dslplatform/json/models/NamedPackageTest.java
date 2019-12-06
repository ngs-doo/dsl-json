package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.models.subpackage.AbstractClass;

public class NamedPackageTest {

	@CompiledJson
	static class ConcreteClass extends AbstractClass {
		public String s;
	}
}