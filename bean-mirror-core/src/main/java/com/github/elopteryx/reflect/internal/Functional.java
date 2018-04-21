package com.github.elopteryx.reflect.internal;

import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.elopteryx.reflect.internal.Utils.wrapper;

public final class Functional {

    private Functional() {
        // No need to instantiate.
        throw new UnsupportedOperationException();
    }

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
