package com.github.elopteryx.reflect.internal;

@FunctionalInterface
interface CheckedSupplier<T> {

    T get() throws Exception;

}
