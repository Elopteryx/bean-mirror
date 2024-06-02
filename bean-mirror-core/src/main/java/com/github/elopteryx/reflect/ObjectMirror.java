package com.github.elopteryx.reflect;

import static com.github.elopteryx.reflect.internal.Utils.isSimilarSignature;
import static com.github.elopteryx.reflect.internal.Utils.types;
import static com.github.elopteryx.reflect.internal.Utils.wrapper;

import com.github.elopteryx.reflect.internal.Functional;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An object based accessor. Works with the
 * given instance and provides methods
 * to access its properties.
 */
public final class ObjectMirror<T> {

    /**
     * The wrapped value. Cannot be null.
     */
    private final T object;

    /**
     * The super type. If defined then it is used
     * instead of the actual wrapped value
     * for accessing fields and methods.
     */
    private final Class<? super T> superType;

    /**
     * The lookup used to hack into the
     * properties of the value. It must be
     * supplied from the client code.
     */
    private final Lookup lookup;

    ObjectMirror(final T object, final Class<? super T> superType, final Lookup lookup) {
        this.object = object;
        this.superType = superType;
        this.lookup = lookup;
    }

    // TYPE

    /**
     * Performs an up cast, allowing to treat the object as one of its
     * ancestor types. This allows field and method lookup from the given
     * super type. Useful if the child type has redefined ('shadowed') a
     * property with the same name.
     * @param clazz The type to be used
     * @param <R> The generic type
     * @return A new mirror instance, using the given type
     */
    @SuppressWarnings("unchecked")
    public <R> ObjectMirror<R> asType(final Class<R> clazz) {
        if (clazz.isAssignableFrom(object.getClass())) {
            return new ObjectMirror<>((R) object, clazz, lookup);
        } else {
            throw new IllegalArgumentException("Not a supertype!");
        }
    }

    /**
     * Returns the type of the current value or its super type
     * if it was supplied.
     * @return The type to be used
     */
    private Class<?> type() {
        if (superType != null) {
            return superType;
        }
        return object.getClass();
    }

    /**
     * Returns the current value.
     * @return The current value
     */
    public T get() {
        return object;
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
    public <R> R get(final String name, final Class<R> clazz) {
        return (R) getField(name, clazz);
    }

    /**
     * Sets the value of the field, identified by its name.
     * @param name The name of the field
     * @param value The new value
     * @return The same mirror instance
     */
    public ObjectMirror<T> set(final String name, final Object value) {
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
    public <R> ObjectMirror<R> field(final String name, final Class<R> clazz) {
        final var result = getField(name, clazz);
        Objects.requireNonNull(result, "Field: " + name);
        return new ObjectMirror<>(clazz.cast(result), null, lookup);
    }

    private Object getField(final String fieldName, final Class<?> fieldType) {
        try {
            final var clazz = type();
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var varHandle = privateLookup.findVarHandle(clazz, fieldName, fieldType);
            return varHandle.get(object);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new BeanMirrorException(e);
        }
    }

    private void setField(final String name, final Object value) {
        try {
            final var clazz = type();
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var handle = privateLookup.findVarHandle(clazz, name, value.getClass());
            handle.set(object, value);
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
    @SuppressWarnings("unchecked")
    public <R> Function<T, R> createGetter(final String name, final Class<R> clazz) {
        final var type = type();
        return Functional.createGetter(name, lookup, (Class<T>) type, clazz);
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
        final var type = type();
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
    @SuppressWarnings("unchecked")
    public <R> BiConsumer<T, R> createSetter(final String name, final Class<R> clazz) {
        final var type = type();
        return Functional.createSetter(name, lookup, (Class<T>) type, clazz);
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
        final var type = type();
        return Functional.createStaticSetter(name, lookup, type, clazz);
    }

    // METHOD

    /**
     * Runs the method of the current object, which is identified
     * by its name and the given arguments. If the method has a return type,
     * then it will be ignored.
     * @param name The name of the method
     * @param args The arguments which will be used for the invocation
     * @return The same mirror instance
     */
    public ObjectMirror<T> run(final String name, final Object... args) {
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
     * Calls the method of the current object, which is identified
     * by its name and the given arguments. The returned value will
     * be wrapped into a new mirror instance, using its type.
     * @param clazz The type of the return value
     * @param name The name of the method
     * @param args The arguments which will be used for the invocation
     * @param <R> The generic type
     * @return A new mirror instance, wrapping the returned value
     */
    @SuppressWarnings("unchecked")
    public <R> ObjectMirror<R> call(final Class<R> clazz, final String name, final Object... args) {
        try {
            final var result = runOrCallMethod(clazz, name, args);
            Objects.requireNonNull(result, "The value returned from the method call is null!");
            return new ObjectMirror<>(((Class<R>)wrapper(clazz)).cast(result), null, lookup);
        } catch (final BeanMirrorException e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    private Object runOrCallMethod(final Class<?> returnType, final String name, final Object... args) throws Throwable {
        final var methodHandle = findMethod(returnType, name, args);
        if (superType == null) {
            final var targetWithArgs = new Object[args.length + 1];
            targetWithArgs[0] = object;
            System.arraycopy(args, 0, targetWithArgs, 1, args.length);
            return methodHandle.invokeWithArguments(targetWithArgs);
        } else {
            return methodHandle.invokeWithArguments(args);
        }
    }

    private MethodHandle findMethod(final Class<?> returnType, final String name, final Object... args) throws Throwable {
        final var type = type();
        final var types = types(args);
        final var privateLookup = MethodHandles.privateLookupIn(type, lookup);
        try {
            if (superType == null) {
                return privateLookup.findVirtual(type, name, MethodType.methodType(returnType, types));
            } else {
                final var method = similarMethod(name, types);
                return privateLookup.unreflectSpecial(method, type).bindTo(object);
            }
        } catch (final NoSuchMethodException e) {
            try {
                return similarMethodHandle(lookup, name, types);
            } catch (final NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    private MethodHandle similarMethodHandle(final Lookup lookup, final String name, final Class<?>... types) throws NoSuchMethodException, IllegalAccessException {
        return lookup.unreflect(similarMethod(name, types));
    }

    private Method similarMethod(final String name, final Class<?>... types) throws NoSuchMethodException {
        final var type = type();

        for (final var method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && isSimilarSignature(method, name, types)) {
                return method;
            }
        }
        for (final var method : type.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && isSimilarSignature(method, name, types)) {
                return method;
            }
        }
        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ObjectMirror<?> om && object.equals(om.object);
    }

    @Override
    public String toString() {
        return object.toString();
    }

}
