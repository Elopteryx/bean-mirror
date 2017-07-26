package com.github.elopteryx.reflect.function;

@FunctionalInterface
public interface LongGetter<T> {

    long apply(T target);
}
