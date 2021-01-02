package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public abstract class PassedDownGeneric {
    @CompiledJson
    public static class ClassExtendingClassWithBoundTypeParameter extends ClassWithBoundTypeParameter<String> {
    }

    @CompiledJson
    public static class ClassWithBoundTypeParameter<T extends String> {

        private T[] property;

        public T[] getProperty() {
            return property;
        }

        public void setProperty(T[] property) {
            this.property = property;
        }

        public List<? extends T> unbounded;

        public Map<String, ? extends BigDecimal> map;

        public T[] array;

        public List<Iface> ii;
    }

    @CompiledJson
    static class Impl implements Iface {
        public int y() { return 0; }
        public void y(int y) { }
    }

    public interface Iface {
        int y();
        void y(int y);
    }
}
