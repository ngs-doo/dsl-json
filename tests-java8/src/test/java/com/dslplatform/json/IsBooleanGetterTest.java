package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IsBooleanGetterTest {

    @CompiledJson
    public static class IsBooleanGetterBeanProperty {

        private final boolean immutablePrimitive;
        private boolean mutablePrimitive;

        public IsBooleanGetterBeanProperty(boolean immutablePrimitive, boolean mutablePrimitive) {
            this.immutablePrimitive = immutablePrimitive;
            this.mutablePrimitive = mutablePrimitive;
        }

        public boolean isImmutablePrimitive() {
            return immutablePrimitive;
        }

        public boolean isMutablePrimitive() {
            return mutablePrimitive;
        }

        public void setMutablePrimitive(boolean mutablePrimitive) {
            this.mutablePrimitive = mutablePrimitive;
        }
    }

    @Test
    public void isBooleanGetterUsedForSerialization() throws IOException {
        IsBooleanGetterBeanProperty isBoolean = new IsBooleanGetterBeanProperty(true, false);
        isBoolean.setMutablePrimitive(true);

        DslJson<Object> dslJson = new DslJson<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(isBoolean, os);
        Assert.assertEquals("{\"immutablePrimitive\":true,\"mutablePrimitive\":true}", os.toString("UTF-8"));
    }

}
