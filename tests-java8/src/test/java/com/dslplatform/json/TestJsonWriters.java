package com.dslplatform.json;

import org.junit.Assert;

import java.util.*;

import static org.junit.Assert.*;

public class TestJsonWriters {
    static abstract class TestJsonWriter extends JsonWriter {
        TestJsonWriter(UnknownSerializer unknownSerializer) {
            super(unknownSerializer);
        }

        TestJsonWriter(int size, UnknownSerializer unknownSerializer) {
            super(size, unknownSerializer);
        }

        TestJsonWriter(byte[] buffer, UnknownSerializer unknownSerializer) {
            super(buffer, unknownSerializer);
        }

        protected final void recordError(Object value) {
            throw new Error("Unexpected call: " + value);
        }
        //This could typically be a record, but it's not supported in Java 8
        static class FilterInfoImpl implements FilterInfo {
            final Object instance;
            final Class<?> clazz;

            FilterInfoImpl(@Nullable Object instance, Class<?> clazz) {
                this.instance = instance;
                this.clazz = clazz;
            }
        }
        private final IdentityHashMap<Object, FilterInfoImpl> filterInfoMap = new IdentityHashMap<>();
        private final IdentityHashMap<FilterInfoImpl, Object> filterInfoReverseMap = new IdentityHashMap<>();

        protected final void validateCommonParams(@Nullable Object instance, Class<?> clazz, FilterInfo _filterInfo) {
            assertNotNull(_filterInfo);
            assertTrue(_filterInfo instanceof FilterInfoImpl);
            final FilterInfoImpl filterInfo = (FilterInfoImpl) _filterInfo;
            if (instance != null) {
                assertSame(filterInfo, filterInfoMap.get(instance));
            }
            assertSame(filterInfo.instance, instance);
            assertSame(filterInfo.clazz, clazz);
        }

        @Override
        public <C> FilterInfo controlledFilterInfo(@Nullable C instance, Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> fields) {
            FilterInfoImpl result = new FilterInfoImpl(instance, clazz);
            if (instance != null) {
                filterInfoMap.put(instance, result);
            }
            filterInfoReverseMap.put(result, instance);
            return result;
        }

    }
    static class AllWriterFactory extends  JsonWriter.Factory {
        static class WriterImpl extends TestJsonWriter {
            WriterImpl(UnknownSerializer unknownSerializer) {
                super(unknownSerializer);
            }

            WriterImpl(int size, UnknownSerializer unknownSerializer) {
                super(size, unknownSerializer);
            }

            WriterImpl(byte[] buffer, UnknownSerializer unknownSerializer) {
                super(buffer, unknownSerializer);
            }
        }
        @Override
        public JsonWriter create(UnknownSerializer unknownSerializer) {
            return new WriterImpl(unknownSerializer);
        }

        @Override
        public JsonWriter create(int size, UnknownSerializer unknownSerializer) {
            return new WriterImpl(size, unknownSerializer);
        }

        @Override
        public JsonWriter create(byte[] buffer, UnknownSerializer unknownSerializer) {
            return new WriterImpl(buffer, unknownSerializer);
        }
    }
    static class NoneWriterFactory extends  JsonWriter.Factory {
        static class WriterImpl extends TestJsonWriter {
            WriterImpl(UnknownSerializer unknownSerializer) {
                super(unknownSerializer);
            }

            WriterImpl(int size, UnknownSerializer unknownSerializer) {
                super(size, unknownSerializer);
            }

            WriterImpl(byte[] buffer, UnknownSerializer unknownSerializer) {
                super(buffer, unknownSerializer);
            }

            @Override
            public <C> List<PropertyInfo<C>> controlledStart(C instance, Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> properties, FilterInfo filterInfo) {
                return Collections.emptyList();
            }
        }
        @Override
        public JsonWriter create(UnknownSerializer unknownSerializer) {
            return new WriterImpl(unknownSerializer);
        }

        @Override
        public JsonWriter create(int size, UnknownSerializer unknownSerializer) {
            return new WriterImpl(size, unknownSerializer);
        }

        @Override
        public JsonWriter create(byte[] buffer, UnknownSerializer unknownSerializer) {
            return new WriterImpl(buffer, unknownSerializer);
        }
    }
    static class SecretWriterFactory extends  JsonWriter.Factory {
        final String fieldName;

        public SecretWriterFactory(String fieldName) {
            super();
            this.fieldName = Objects.requireNonNull(fieldName);
        }

        static class WriterImpl extends TestJsonWriter {
            private final SecretWriterFactory parent;

            WriterImpl(SecretWriterFactory parent, UnknownSerializer unknownSerializer) {
                super(unknownSerializer);
                this.parent = parent;
            }

            WriterImpl(SecretWriterFactory parent, int size, UnknownSerializer unknownSerializer) {
                super(size, unknownSerializer);
                this.parent = parent;
            }

            WriterImpl(SecretWriterFactory parent, byte[] buffer, UnknownSerializer unknownSerializer) {
                super(buffer, unknownSerializer);
                this.parent = parent;
            }

            @Override
            public <C> JsonWriter controlledPrepareForProperty(C instance, Class<C> clazz, PropertyAccessor<C> access, FilterInfo filterInfo, PropertyInfo<C> property, JsonWriter writer) {
                super.controlledPrepareForProperty(instance, clazz, access, filterInfo, property, writer);
                if (parent.fieldName.equals(property.getName())) {
                    writeString("That's a secret!");
                    return null;
                }
                return this;
            }
        }
        @Override
        public JsonWriter create(UnknownSerializer unknownSerializer) {
            return new WriterImpl(this, unknownSerializer);
        }

        @Override
        public JsonWriter create(int size, UnknownSerializer unknownSerializer) {
            return new WriterImpl(this, size, unknownSerializer);
        }

        @Override
        public JsonWriter create(byte[] buffer, UnknownSerializer unknownSerializer) {
            return new WriterImpl(this, buffer, unknownSerializer);
        }
    }

}
