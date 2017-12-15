package com.github.elopteryx.reflect.internal;

import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Accessor {

    static Accessor of(Lookup lookup) {
        return new AccessorImpl(lookup);
    }

    /**
     * Get a wrapper type for a primitive type, or the argument type itself, if
     * it is not a primitive type.
     * @param type The class type to be wrapped
     * @return The wrapped type, or null
     */
    static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    // CONSTRUCTORS

    Object useConstructor(Class<?> clazz, Object... args);

    // FIELDS

    Object getField(Object object, String name, Class<?> fieldType);

    void setField(Object object, String name, Class<?> fieldType, Object value);

    <T, R> Function<T, R> createGetter(Object target, String name, Class<R> clazz);

    <T, R> Supplier<R> createStaticGetter(Class<T> target, String name, Class<R> clazz);

    <T, R> BiConsumer<T, R> createSetter(Object target, String name, Class<R> clazz);

    <T, R> Consumer<R> createStaticSetter(Class<T> target, String name, Class<R> clazz);

    // METHODS

    void runMethod(Object object, String name, Object... args) throws BeanMirrorException;

    Object callMethod(Object object, String name, Object... args) throws BeanMirrorException;
}
