package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.internal.Accessor;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

/**
 * A generic wrapper class for accessing the given instance.
 * @param <T> The generic type for the current value.
 */
public class BeanMirror<T> {

    /**
     * The wrapped value. Cannot be null.
     */
    private final T object;

    /**
     * The accessor used to reflect into the
     * properties of the value.
     */
    private final Accessor accessor;

    private BeanMirror(T object, Accessor accessor) {
        this.object = object;
        this.accessor = accessor;
    }

    public static <R> BeanMirror<R> of(R object) {
        Objects.requireNonNull(object);
        return new BeanMirror<>(object, new Accessor(null));
    }

    public static <R> BeanMirror<R> of(R object, Lookup lookup) {
        Objects.requireNonNull(object);
        return new BeanMirror<>(object, new Accessor(lookup));
    }

    // VALUE

    public T get() {
        return object;
    }

    public Class<?> type() {
        return object instanceof Class ? (Class<?>)object : object.getClass();
    }

    // CONSTRUCTORS

    public BeanMirror<T> create(Object... args) {
        @SuppressWarnings("unchecked")
        final T result = (T)accessor.useConstructor(type(), args);
        Objects.requireNonNull(result, "");
        return new BeanMirror<>(result, accessor);
    }

    // FIELDS

    public Object get(String name) {
        return field(name).get();
    }

    public <R> R get(String name, Class<R> clazz) {
        return field(name, clazz).get();
    }

    public BeanMirror<T> set(String name, Object value) {
        accessor.setField(object, name, value);
        return this;
    }

    public BeanMirror<Object> field(String name) {
        return field(name, Object.class);
    }

    public <R> BeanMirror<R> field(String name, Class<R> clazz) {
        final Object result = accessor.getField(object, name);
        Objects.requireNonNull(result, "");
        return new BeanMirror<>(clazz.cast(result), accessor);
    }

    // METHODS

    public BeanMirror<T> run(String name, Object... args) {
        accessor.callMethod(object, name, args);
        return this;
    }

    public BeanMirror<Object> call(String name, Object... args) {
        return call(Object.class, name, args);
    }

    public <R> BeanMirror<R> call(Class<R> clazz, String name, Object... args) {
        final Object result = accessor.callMethod(object, name, args);
        Objects.requireNonNull(result, "");
        return new BeanMirror<>(clazz.cast(result), accessor);
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BeanMirror && object.equals(((BeanMirror) obj).get());
    }

    @Override
    public String toString() {
        return object.toString();
    }

}
