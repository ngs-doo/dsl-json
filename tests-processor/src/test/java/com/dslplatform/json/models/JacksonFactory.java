package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonCreator;

public class JacksonFactory {

	@CompiledJson
	public static class Reference {
		public Factory factory;
	}

	public static class Factory {
		private int x;
		private int y;

		private Factory(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@JsonCreator
		public static Factory New(int x, int y) {
			return new Factory(x, y);
		}

		public int x() {
			return x;
		}
		public int y() {
			return y;
		}
	}
}