package com.dslplatform.json.runtime;

import com.dslplatform.json.SerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract class Reflection {

	static final class ReadField implements Function {
		private final Field field;

		ReadField(Field field) {
			this.field = field;
		}

		@Override
		public Object apply(Object instance) {
			try {
				return field.get(instance);
			} catch (IllegalAccessException e) {
				throw new SerializationException("Unable to read field " + field.getName() + " of " + field.getDeclaringClass(), e);
			}
		}
	}

	static final class ReadMethod implements Function {
		private final Method method;

		ReadMethod(Method method) {
			this.method = method;
		}

		@Override
		public Object apply(Object instance) {
			try {
				return method.invoke(instance);
			} catch (Exception e) {
				throw new SerializationException("Unable to call method " + method.getName() + " of " + method.getDeclaringClass(), e);
			}
		}
	}

	static final class SetField implements BiConsumer {
		private final Field field;

		SetField(Field field) {
			this.field = field;
		}

		@Override
		public void accept(Object instance, Object value) {
			try {
				field.set(instance, value);
			} catch (IllegalAccessException e) {
				throw new SerializationException("Unable to set field " + field.getName() + " of " + field.getDeclaringClass(), e);
			}
		}
	}

	static final class SetMethod implements BiConsumer {
		private final Method method;

		SetMethod(Method method) {
			this.method = method;
		}

		@Override
		public void accept(Object instance, Object value) {
			try {
				method.invoke(instance, value);
			} catch (Exception e) {
				throw new SerializationException("Unable to call method " + method.getName() + " of " + method.getDeclaringClass(), e);
			}
        }
	}
}
