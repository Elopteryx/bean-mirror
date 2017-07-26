package com.github.elopteryx.reflect.function;

@FunctionalInterface
public interface Getter<T, V> {

    V apply(T target);
}
