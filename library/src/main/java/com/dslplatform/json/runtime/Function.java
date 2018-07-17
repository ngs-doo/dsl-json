package com.dslplatform.json.runtime;

public interface Function<T, R> {
    R apply(T t);
}
