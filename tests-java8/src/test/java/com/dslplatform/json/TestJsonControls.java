package com.dslplatform.json;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestJsonControls {
    private TestJsonControls() {
    }

    static class AllControls extends JsonControls<ControlInfo> {

        public static final JsonControls.Factory<AllControls> FACTORY = JsonControls.SingletonFactory.of(new AllControls());

        @Override
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return ControlInfo.BLANK;
        }

    }

    static class NoneControls extends JsonControls<ControlInfo> {
        public static final JsonControls.Factory<NoneControls> FACTORY = JsonControls.SingletonFactory.of(new NoneControls());

        @Override
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return ControlInfo.BLANK;
        }

        @Override
        public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo) {
            return Collections.emptyList();
        }
        //we dont need to override the write methods as we are not writing anything
    }

    static class SecretControls extends JsonControls<ControlInfo> {
        public static JsonControls.Factory<SecretControls> factoryFor(String field)  {
            return new JsonControls.Factory<SecretControls>() {
                @Override
                public SecretControls createFor(JsonWriter writer) {
                    return new SecretControls(field);
                }
            };
        }

        private final String secretField;

        public SecretControls(String secretField) {
            this.secretField = secretField;
        }

        @Override
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return ControlInfo.BLANK;
        }

        @Override
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, boolean checkDefaults, boolean isNotDefaultValue) {
            if (propertyInfo.getName().equals(secretField)) {
                writer.writeAscii(propertyInfo.getQuotedNameAndColon());
                writer.writeString("That's a secret!");
                return PropertyWriteControl.WRITTEN_DIRECTLY;
            }
            return PropertyWriteControl.WRITE_NORMALLY;
        }
        @Override
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, String value, boolean checkDefaults, boolean isNotDefaultValue) {
            if (propertyInfo.getName().equals(secretField)) {
                writer.writeAscii(propertyInfo.getQuotedNameAndColon());
                writer.writeString("That's a secret!");
                return PropertyWriteControl.WRITTEN_DIRECTLY;
            }
            return PropertyWriteControl.WRITE_NORMALLY;
        }

    }

    static class ComplexInfo extends ControlInfo {

        final List<PropertyInfo> ordered;
        final Set<String> secret;
        final Set<String> dodgy;

        public ComplexInfo(List<PropertyInfo> ordered, Set<String> secret, Set<String> dodgy) {
            this.ordered = ordered;
            this.secret = secret;
            this.dodgy = dodgy;
        }

        public static <C> ComplexInfo create(ClassInfo<C> classInfo) {
            List<PropertyInfo> ordered = classInfo.getPropertyInfos().stream().filter(p -> !p.getName().startsWith("private"))
                    .sorted(Comparator.comparing(PropertyInfo::getName)).collect(Collectors.toList());
            Set<String> secret = classInfo.getPropertyInfos().stream().map(PropertyInfo::getName).filter(name -> name.startsWith("secret")).collect(Collectors.toSet());
            Set<String> dodgy = classInfo.getPropertyInfos().stream().map(PropertyInfo::getName).filter(name -> name.startsWith("dodgy")).collect(Collectors.toSet());
            return new ComplexInfo(ordered, secret, dodgy);

        }
    }

    static class ComplexControls extends JsonControls<ComplexInfo> {
        public static final JsonControls.Factory<ComplexControls> FACTORY = new JsonControls.Factory<ComplexControls>() {
            private final ConcurrentHashMap<Class<?>, ComplexInfo> classCache = new ConcurrentHashMap<>();
            private final Pattern secretPattern = Pattern.compile("password|secret|key|token|credentials", Pattern.CASE_INSENSITIVE);
            @Override
            public ComplexControls createFor(JsonWriter writer) {
                return new ComplexControls(writer, classCache, secretPattern.matcher(""));
            }

        };
        private final JsonWriter writer;
        private final ConcurrentHashMap<Class<?>, ComplexInfo> classCache;
        private final Matcher matcher;
        private final ReusableCharSequenceView charView;
        private final int safeUntil;

        public ComplexControls(JsonWriter writer, ConcurrentHashMap<Class<?>, ComplexInfo> classCache, Matcher matcher) {
            this.writer = writer;
            this.classCache = classCache;
            this.matcher = matcher;
            this.charView = new ReusableCharSequenceView();
            this.safeUntil = 0;
        }

        private <C> ComplexInfo getOrCompute(ClassInfo<C> classInfo) {
            return classCache.computeIfAbsent(classInfo.getType(), k -> ComplexInfo.create(classInfo));
        }

        @Override
        public <C> ComplexInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return getOrCompute(classInfo);
        }

        @Override
        public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo) {
            return controlledInfo.ordered;
        }

        @Override
        public <C> PropertyWriteControl shouldWriteCommon(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, boolean checkDefaults, boolean isNotDefaultValue) {
            if (controlledInfo.secret.contains(propertyInfo.getName())) {
                writer.writeAscii(propertyInfo.getQuotedNameAndColon());
                writer.writeString("That's a secret!");
                return PropertyWriteControl.WRITTEN_DIRECTLY;
            }
            return PropertyWriteControl.WRITE_NORMALLY;
        }

        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, String value, int positionBefore) {
            matcher.reset(value);
            if (matcher.find()) {
                writer.rewind(positionBefore);
                writer.writeNull();
            }
            if (controlledInfo.dodgy.contains(propertyInfo.getName())) {
                // TODO: test some dodgy stuff
            }

        }
        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, int positionBefore) {
            charView.reset(writer, positionBefore);
            matcher.reset(charView);
            if (matcher.find()) {
                writer.rewind(positionBefore);
                writer.writeNull();
            }
            if (controlledInfo.dodgy.contains(propertyInfo.getName())) {
                // TODO: test some dodgy stuff
            }

        }
    }
    private static class ReusableCharSequenceView implements CharSequence {
        private JsonWriter underlying;
        private byte[] buffer;
        private int end;
        private int start;

        ReusableCharSequenceView reset(JsonWriter underlying, int start) {
            return reset( underlying, start, underlying.size());
        }
        ReusableCharSequenceView reset(JsonWriter underlying, int start, int end) {
            this.underlying = underlying;
            this.buffer = underlying.getByteBuffer();
            this.start = start;
            this.end = end;
            return this;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return (char) buffer[start + index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new ReusableCharSequenceView().reset(underlying, this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            return new String(buffer, start, length(), StandardCharsets.UTF_8);
        }
    }
}
