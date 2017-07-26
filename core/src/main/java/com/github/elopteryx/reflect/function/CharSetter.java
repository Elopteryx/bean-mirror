package com.github.elopteryx.reflect.function;

@FunctionalInterface
public interface CharSetter<T> {

    void accept(T target, char value);

}
