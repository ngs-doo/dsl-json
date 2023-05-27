package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class ImmutableInheritance {

    @CompiledJson
    public static abstract class BaseFields {

        private final String string;

        protected BaseFields(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    @CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
    public static class MainFieldsWithConstant extends BaseFields {

        private final int integer;

        public MainFieldsWithConstant(int integer) {
            super("name");
            this.integer = integer;
        }

        public int getInteger() {
            return integer;
        }
    }

    @CompiledJson
    public static class MainFieldsWithPassThrough extends BaseFields {

        private final int integer;

        public MainFieldsWithPassThrough(int integer, String string) {
            super(string);
            this.integer = integer;
        }

        public int getInteger() {
            return integer;
        }
    }
}