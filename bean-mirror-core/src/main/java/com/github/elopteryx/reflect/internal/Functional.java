package com.github.elopteryx.reflect.internal;

import static com.github.elopteryx.reflect.internal.Utils.wrapper;

import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Functional {

    private Functional() {
        // No need to instantiate.
        throw new UnsupportedOperationException();
    }

    /**
     * Getter creator method used by both Mirror implementations.
     * @param name The field name
     * @param lookup The lookup used for access check
     * @param targetType The class type
     * @param returnType The field type
     * @param <T> Generic param for the class
     * @param <R> Generic param for the field
     * @return A new function
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Function<T, R> createGetter(String name, MethodHandles.Lookup lookup, Class<T> targetType, Class<R> returnType) {
        try {
            final var field = targetType.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final MethodHandles.Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(targetType, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findVarHandle(targetType, name, returnType);
            final var classToUse = (Class<R>) wrapper(returnType);
            return obj -> classToUse.cast(varHandle.get(obj));
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    /**
     * Static etter creator method used by both Mirror implementations.
     * @param name The field name
     * @param lookup The lookup used for access check
     * @param targetType The class type
     * @param returnType The field type
     * @param <T> Generic param for the class
     * @param <R> Generic param for the field
     * @return A new supplier
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Supplier<R> createStaticGetter(String name, MethodHandles.Lookup lookup, Class<T> targetType, Class<R> returnType) {
        try {
            final var field = targetType.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final MethodHandles.Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(targetType, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findStaticVarHandle(targetType, name, returnType);
            final var classToUse = (Class<R>) wrapper(returnType);
            return () -> classToUse.cast(varHandle.get());
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    /**
     * Setter creator method used by both Mirror implementations.
     * @param name The field name
     * @param lookup The lookup used for access check
     * @param targetType The class type
     * @param returnType The field type
     * @param <T> Generic param for the class
     * @param <R> Generic param for the field
     * @return A new bi-consumer
     */
    @SuppressWarnings("unchecked")
    public static <T, R> BiConsumer<T, R> createSetter(String name, MethodHandles.Lookup lookup, Class<T> targetType, Class<R> returnType) {
        try {
            final var field = targetType.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final MethodHandles.Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(targetType, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findVarHandle(targetType, name, returnType);
            return (target, value) -> varHandle.set((T)target, (R)value);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    /**
     * Static setter creator method used by both Mirror implementations.
     * @param name The field name
     * @param lookup The lookup used for access check
     * @param targetType The class type
     * @param returnType The field type
     * @param <T> Generic param for the class
     * @param <R> Generic param for the field
     * @return A new consumer
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Consumer<R> createStaticSetter(String name, MethodHandles.Lookup lookup, Class<T> targetType, Class<R> returnType) {
        try {
            final var field = targetType.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final MethodHandles.Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(targetType, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findStaticVarHandle(targetType, name, returnType);
            return value -> varHandle.set((R)value);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }
}
