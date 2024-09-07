package com.dslplatform.json;

import org.junit.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    /** demonstrates a more complex handling of the json writing, with a few of the facilities wire together
     * where
     * 1. The fields are ordered alphabetically
     * 2. any field names that start with &quot;private&quot; are not written
     * 3. any field names that start with &quot;secret&quot; have a value written of &quot;That's a secret!&quot;
     * 4. Any string field value that contains the word &quot;password&quot; is replaced with null;
     * 5. List are treated like fields in the above rules
     * 6. Maps are treated like field names and values in the above rules
     * 7. If a field name starts with &quot;dodgy&quot;, the field is written if the value (in json form) doesnt contain
     * &quot;bad&quot; otherwise is replaced with null
     */
    static class ComplexWriterFactory extends  JsonWriter.Factory {

        static class WriterImpl extends TestJsonWriter {
            private final ComplexWriterFactory parent;

            WriterImpl(ComplexWriterFactory parent, UnknownSerializer unknownSerializer) {
                super(unknownSerializer);
                this.parent = parent;
            }

            WriterImpl(ComplexWriterFactory parent, int size, UnknownSerializer unknownSerializer) {
                super(size, unknownSerializer);
                this.parent = parent;
            }

            WriterImpl(ComplexWriterFactory parent, byte[] buffer, UnknownSerializer unknownSerializer) {
                super(buffer, unknownSerializer);
                this.parent = parent;
            }
            static class ClassData<C> implements FilterInfo {

                final List<PropertyInfo<C>> ordered;
                final Set<String> secret;
                final Set<String> dodgy;

                public ClassData(List<PropertyInfo<C>> ordered, Set<String> secret, Set<String> dodgy) {
                    this.ordered = ordered;
                    this.secret = secret;
                    this.dodgy = dodgy;
                }

                public static <C> ClassData<C> create(Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> fields) {
                    List<PropertyInfo<C>> ordered = fields.stream().filter(p -> !p.getName().startsWith("private"))
                            .sorted(Comparator.comparing(PropertyInfo::getName)).collect(Collectors.toList());
                    //make sure that we have commas between the fields, and no comma for the first field
                    for (int i = 0; i < ordered.size(); i++) {
                        ordered.set(i, ordered.get(i).asFirstProperty(i == 0));
                    }
                    Set<String> secret = fields.stream().map(PropertyInfo::getName).filter(name -> name.startsWith("secret")).collect(Collectors.toSet());
                    Set<String> dodgy = fields.stream().map(PropertyInfo::getName).filter(name -> name.startsWith("dodgy")).collect(Collectors.toSet());
                    return new ClassData<C>(ordered, secret, dodgy);

                }
            }
            private final ConcurrentHashMap<Class<?>, ClassData<?>> classCache = new ConcurrentHashMap<>();

            private <C> ClassData<C> getOrCompute(Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> fields) {
                //noinspection unchecked
                return (ClassData<C>)classCache.computeIfAbsent(clazz, k -> ClassData.create(clazz, access, fields));
            }
            
            @Override
            public <C> FilterInfo controlledFilterInfo(C instance, Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> fields) {
                return getOrCompute(clazz, access, fields);
            }


            @Override
            public <C> List<PropertyInfo<C>> controlledStart(C instance, Class<C> clazz, PropertyAccessor<C> access, List<PropertyInfo<C>> properties, FilterInfo filterInfo) {
                return classData(clazz, filterInfo).ordered;
            }

            private <C> ClassData<C> classData( Class<C> clazz, FilterInfo filterInfo) {
                //noinspection unchecked
                return (ClassData<C>)filterInfo;
            }

            @Override
            public <C> @Nullable JsonWriter controlledPrepareForProperty(C instance, Class<C> clazz, PropertyAccessor<C> access, FilterInfo filterInfo, PropertyInfo<C> property, @Nullable JsonWriter writer) {
                handleDodgy(writer);
                writeAscii(property.getQuoted());

                ClassData<C> classData = classData(clazz, filterInfo);
                if (classData.secret.contains(property.getName())) {
                    writeString("That's a secret!");
                    return null;
                }

                if (classData.dodgy.contains(property.getName())) {
                    return new JsonWriter(this.unknownSerializer);
                }

                return this;
            }

            //handles a child writer, and looks for some bad values in the json
            private void handleDodgy(@Nullable JsonWriter writer) {
                if (writer != null && writer != this) {
                    String value = new String(writer.getByteBuffer(), 0, writer.size());
                    if (value.contains("bad")) {
                        this.writeNull();
                    } else {
                        this.writeRaw(writer.getByteBuffer(), 0, writer.size());
                    }
                }
            }

            @Override
            public <C> void controlledFinished(C instance, Class<C> clazz, PropertyAccessor<C> access, FilterInfo filterInfo, JsonWriter writer) {
                handleDodgy(writer);
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
