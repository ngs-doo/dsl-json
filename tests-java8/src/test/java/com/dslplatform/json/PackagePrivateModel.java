package com.dslplatform.json;

@CompiledJson
class PackagePrivateModel {
	public int x;

	@CompiledJson
	static class Something {
		public int y;
	}
}
