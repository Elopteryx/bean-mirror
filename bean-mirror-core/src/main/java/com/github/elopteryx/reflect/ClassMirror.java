package com.github.elopteryx.reflect;

import static com.github.elopteryx.reflect.internal.Utils.isSimilarSignature;
import static com.github.elopteryx.reflect.internal.Utils.types;
import static java.lang.invoke.MethodType.methodType;

import com.github.elopteryx.reflect.internal.Functional;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class based accessor. Works with a given
 * class type and provides methods to create
 * new instances and access static properties.
 */
public final class ClassMirror<T> {

    /**
     * The wrapped value. Cannot be null.
     */
    private final Class<T> clazz;

    /**
     * The lookup used to hack into the
     * properties of the value. It must be
     * supplied from the client code.
     */
    private final Lookup lookup;

    ClassMirror(final Class<T> clazz, final Lookup lookup) {
        this.clazz = clazz;
        this.lookup = lookup;
    }

    // CONSTRUCTOR

    /**
     * Creates a new instance from the current type. Returns it
     * wrapped into the mirror.
     * @param args The constructor arguments
     * @return A new mirror instance, wrapping the created object
     */
    @SuppressWarnings("unchecked")
    public ObjectMirror<T> create(final Object... args)  {
        try {
            final T result = (T)useConstructor(args);
            return new ObjectMirror<>(result, null, lookup);
        } catch (final Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    private Object useConstructor(final Object... args) {
        try {
            final var types = types(args);

            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

            final var constructorHandle = privateLookup.findConstructor(clazz, methodType(void.class, types));
            return constructorHandle.invokeWithArguments(args);

        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // FIELD

    /**
     * Gets the value of the field, identified by its name.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return The value of the field
     */
    @SuppressWarnings("unchecked")
    public <R> R getStatic(final String name, final Class<R> clazz) {
        return (R)getField(name, clazz);
    }

    /**
     * Sets the value of the field, identified by its name.
     * @param name The name of the field
     * @param value The new value
     * @return The same mirror instance
     */
    public ClassMirror<T> setStatic(final String name, final Object value) {
        setField(name, value);
        return this;
    }

    /**
     * Switches over to the field, identified by its name.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new mirror instance, wrapping the field
     */
    public <R> ObjectMirror<R> staticField(final String name, final Class<R> clazz) {
        final var result = getField(name, clazz);
        Objects.requireNonNull(result, "Field: " + name);
        return new ObjectMirror<>(clazz.cast(result), null, lookup);
    }

    private Object getField(final String fieldName, final Class<?> fieldType) {
        try {
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var varHandle = privateLookup.findStaticVarHandle(clazz, fieldName, fieldType);
            return varHandle.get();
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new BeanMirrorException(e);
        }
    }

    private void setField(final String name, final Object value) {
        try {
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var handle = privateLookup.findStaticVarHandle(clazz, name, value.getClass());
            handle.set(value);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new BeanMirrorException(e);
        }
    }

    /**
     * Creates a new function which can be used to get the value of
     * field for the object given to the function. The input
     * type will be the same as the current type, the output type will
     * be the same as the given field class type.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new Function
     */
    public <R> Function<T, R> createGetter(final String name, final Class<R> clazz) {
        final var type = this.clazz;
        return Functional.createGetter(name, lookup, type, clazz);
    }

    /**
     * Creates a new supplier which can be used to get the value of
     * the static field for the current type. The return
     * type will be the same as the given class type.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new Supplier
     */
    public <R> Supplier<R> createStaticGetter(final String name, final Class<R> clazz) {
        final var type = this.clazz;
        return Functional.createStaticGetter(name, lookup, type, clazz);
    }

    /**
     * Creates a new bi-consumer which can be used to set the value of
     * field for the object given to the function. The first input
     * type will be the same as the current type, the output type will
     * be the same as the given field class type.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new BiConsumer
     */
    public <R> BiConsumer<T, R> createSetter(final String name, final Class<R> clazz) {
        final var type = this.clazz;
        return Functional.createSetter(name, lookup, type, clazz);
    }

    /**
     * Creates a new consumer which can be used to set the value of
     * the static field for the current type. The input
     * type will be the same as the given class type.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new Consumer
     */
    public <R> Consumer<R> createStaticSetter(final String name, final Class<R> clazz) {
        final var type = this.clazz;
        return Functional.createStaticSetter(name, lookup, type, clazz);
    }

    // METHOD

    /**
     * Runs the static method of the current class, which is identified
     * by its name and the given arguments. If the method has a return type,
     * then it will be ignored.
     * @param name The name of the method
     * @param args The arguments which will be used for the invocation
     * @return The same mirror instance
     */
    public ClassMirror<T> runStatic(final String name, final Object... args) {
        try {
            runOrCallMethod(void.class, name, args);
            return this;
        } catch (final BeanMirrorException e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    /**
     * Calls the static method of the current object, which is identified
     * by its name and the given arguments. The returned value will
     * be wrapped into a new mirror instance, using its type.
     * @param clazz The type of the return value
     * @param name The name of the method
     * @param args The arguments which will be used for the invocation
     * @param <R> The generic type
     * @return A new mirror instance, wrapping the returned value
     */
    public <R> ObjectMirror<R> callStatic(final Class<R> clazz, final String name, final Object... args) {
        try {
            final var result = runOrCallMethod(clazz, name, args);
            Objects.requireNonNull(result, "The value returned from the method call is null!");
            return new ObjectMirror<>(clazz.cast(result), null, lookup);
        } catch (final BeanMirrorException e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    private Object runOrCallMethod(final Class<?> returnType, final String name, final Object... args) throws Throwable {
        final var methodHandle = findMethod(returnType, name, args);
        return methodHandle.invokeWithArguments(args);
    }

    private MethodHandle findMethod(final Class<?> returnType, final String name, final Object... args) throws Throwable {
        final var types = types(args);
        final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
        try {
            return privateLookup.findStatic(clazz, name, methodType(returnType, types));
        } catch (final NoSuchMethodException e) {
            try {
                return similarMethod(lookup, name, types);
            } catch (final NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    private MethodHandle similarMethod(final Lookup lookup, final String name, final Class<?>... types) throws NoSuchMethodException, IllegalAccessException {
        for (final var method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && isSimilarSignature(method, name, types)) {
                return lookup.unreflect(method);
            }
        }
        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + clazz + ".");
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ClassMirror && clazz.equals(((ClassMirror<?>) obj).clazz);
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
