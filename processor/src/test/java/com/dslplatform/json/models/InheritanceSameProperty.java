package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

public class InheritanceSameProperty {

    @CompiledJson
    public static class BaseFields {

        public String string;

        public String getString() { return string; }
        public void setString(String value) { string = value; }
    }

    @CompiledJson
    public static class MasterClass extends BaseFields {

        @JsonAttribute(name = "named_fields")
        @Override
        public String getString() {
            return super.getString();
        }
    }
}