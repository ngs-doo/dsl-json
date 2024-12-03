package com.dslplatform.json;

import java.util.List;

public abstract class JsonControls<T extends ControlInfo> {

    protected JsonControls() {
    }

    public abstract <C> T controlledInfo(C instance, ClassInfo<C> classInfo);

    public <C> List<PropertyInfo> controlledProperties(C instance, ClassInfo<C> classInfo, T controlledInfo){
        return classInfo.getPropertyInfos();
    }

    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final long value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final int value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final short value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final byte value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final double value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final float value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final boolean value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final Object value, final boolean checkDefaults, final boolean isNotDefaultValue) {
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }
    //overload for String as its such a common case. Maybe consider others for boxed values, or other common cases that are written
    public <C> PropertyWriteControl shouldWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final String value, final boolean checkDefaults, final boolean isNotDefaultValue){
        return shouldWriteCommon(instance, classInfo, controlledInfo, propertyInfo, writer, checkDefaults, isNotDefaultValue);
    }

    public <C> PropertyWriteControl shouldWriteCommon(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final boolean checkDefaults, final boolean isNotDefaultValue) {
        return PropertyWriteControl.WRITE_NORMALLY;
    }

    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final long value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final int value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final short value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final byte value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final double value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final float value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final boolean value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final Object value, int positionBeforeValue) {    }
    public <C> void afterPropertyWrite(C instance, ClassInfo<C> classInfo, T controlledInfo, PropertyInfo propertyInfo, JsonWriter writer, final String value, int positionBeforeValue) {    }


    public abstract static class Factory<T extends JsonControls<? extends ControlInfo>> {
        public abstract T createFor(JsonWriter writer);
    }
    public static class SingletonFactory<T extends JsonControls<? extends ControlInfo>> extends Factory<T> {
        private final T instance;

        private SingletonFactory(T instance) {
            this.instance = instance;
        }

        public static <T extends JsonControls<? extends ControlInfo>> SingletonFactory<T> of(T instance) {
            return new SingletonFactory<T>(instance);
        }

        public T createFor(JsonWriter writer) {
            return instance;
        }
    }

}