package com.github.elopteryx.reflect.function;

@FunctionalInterface
public interface Setter<T, V> {

    void accept(T target, V value);

}
