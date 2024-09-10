package com.dslplatform.json;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TestJsonControls {
    private TestJsonControls() {
    }

    static class AllControls extends JsonControls<ControlInfo> {

        @Override
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return null;
        }

        @Override
        public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo) {
            return classInfo.getPropertyInfos();
        }

        @Override
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, boolean checkDefaults, boolean isNotDefaultValue) {
            return PropertyWriteControl.WRITE_NORMALLY;
        }

        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore) {

        }
    }

    static class NoneControls extends JsonControls<ControlInfo> {

        @Override
        
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return null;
        }

        @Override
        public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo) {
            return Collections.emptyList();
        }

        @Override
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, boolean checkDefaults, boolean isNotDefaultValue) {
            return PropertyWriteControl.IGNORED;
        }

        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore) {

        }
    }

    static class SecretControls extends JsonControls<ControlInfo> {

        private final String secretField;

        public SecretControls(String secretField) {
            this.secretField = secretField;
        }

        @Override
        public <C> ControlInfo controlledInfo(C instance, ClassInfo<C> classInfo) {
            return null;
        }

        @Override
        public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo) {
            return Collections.emptyList();
        }

        @Override
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, boolean checkDefaults, boolean isNotDefaultValue) {
            return propertyInfo.getName().equals(secretField) ? PropertyWriteControl.IGNORED : PropertyWriteControl.WRITE_NORMALLY;
        }

        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ControlInfo controlInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore) {

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
        private final ConcurrentHashMap<Class<?>, ComplexInfo> classCache = new ConcurrentHashMap<>();

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
        public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, Object value, boolean checkDefaults, boolean isNotDefaultValue) {
            if (controlledInfo.secret.contains(propertyInfo.getName())) {
                writer.writeAscii(propertyInfo.getQuotedNameAndColon());
                writer.writeString("That's a secret!");
                return PropertyWriteControl.WRITTEN_DIRECTLY;
            }
            return PropertyWriteControl.WRITE_NORMALLY;
        }

        @Override
        public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, ComplexInfo controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, long positionBefore) {
            if (controlledInfo.dodgy.contains(propertyInfo.getName())) {
               // ....
            }

        }
    }
}
