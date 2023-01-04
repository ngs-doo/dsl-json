package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
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

    @CompiledJson
    public static final class WithIs {
        private final boolean isLocked;
        private final String token;
        private final boolean isJustLocked;
        private final boolean isAnnotation;

        public WithIs(boolean isLocked, String token, boolean justLocked, boolean isAnnotation) {
            this.isLocked = isLocked;
            this.token = token;
            this.isJustLocked = justLocked;
            this.isAnnotation = isAnnotation;
        }

        public final boolean isLocked() {
            return isLocked;
        }

        public final String getToken() {
            return token;
        }

        public final boolean isJustLocked() {
            return isJustLocked;
        }

        @JsonAttribute(name = "annotation")
        public final boolean isAnnotation() {
            return isAnnotation;
        }
    }

    @Test
    public void isAndConstructor() throws IOException {
        WithIs wi = new WithIs(true, "abc", true, true);

        DslJson<Object> dslJson = new DslJson<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(wi, os);
        Assert.assertEquals("{\"isLocked\":true,\"token\":\"abc\",\"justLocked\":true,\"annotation\":true}", os.toString("UTF-8"));

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        WithIs result = dslJson.deserialize(WithIs.class, is);
        Assert.assertTrue(result.isLocked());
        Assert.assertEquals("abc", result.getToken());
        Assert.assertTrue(result.isJustLocked());
        Assert.assertTrue(result.isAnnotation());
    }
}
