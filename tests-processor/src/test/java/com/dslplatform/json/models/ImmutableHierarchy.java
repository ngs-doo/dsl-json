package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

public class ImmutableHierarchy {

    @CompiledJson
    public static abstract class Person {

        private final String name;

        protected Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @CompiledJson
    public abstract static class Parent extends Person {

        public Parent(String name) {
            super(name);
        }
    }

    @CompiledJson
    public static class Mother extends Parent {

        @CompiledJson
        public Mother(String name) {
            super(name);
        }
    }

    @CompiledJson
    public static class Father extends Parent {

        @CompiledJson
        public Father(String name) {
            super(name);
        }
    }
}