package com.github.elopteryx.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.elopteryx.reflect.internal.Utils.isSimilarSignature;
import static com.github.elopteryx.reflect.internal.Utils.types;
import static com.github.elopteryx.reflect.internal.Utils.wrapper;
import static java.lang.invoke.MethodType.methodType;

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

    ClassMirror(Class<T> clazz, Lookup lookup) {
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
    public ObjectMirror<T> create(Object... args)  {
        final T result;
        try {
            result = (T)useConstructor(args);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
        return new ObjectMirror<>(result, null, lookup);
    }

    private Object useConstructor(Object... args) {
        try {
            final var types = types(args);

            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

            final var constructorHandle = privateLookup.findConstructor(clazz, methodType(void.class, types));
            return constructorHandle.invokeWithArguments(args);

        } catch (Throwable e) {
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
    public <R> R getStatic(String name, Class<R> clazz) {
        return (R)getField(name, clazz);
    }

    /**
     * Sets the value of the field, identified by its name.
     * @param name The name of the field
     * @param value The new value
     * @return The same mirror instance
     */
    public ClassMirror<T> setStatic(String name, Object value) {
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
    public <R> ObjectMirror<R> staticField(String name, Class<R> clazz) {
        final var result = getField(name, clazz);
        Objects.requireNonNull(result, "Field: " + name);
        return new ObjectMirror<>(clazz.cast(result), null, lookup);
    }

    private Object getField(String fieldName, Class<?> fieldType) {
        try {
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var varHandle = privateLookup.findStaticVarHandle(clazz, fieldName, fieldType);
            return varHandle.get();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(String name, Object value) {
        try {
            final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final var handle = privateLookup.findStaticVarHandle(clazz, name, value.getClass());
            handle.set(value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
    @SuppressWarnings("unchecked")
    public <R> Supplier<R> createStaticGetter(String name, Class<R> clazz) {
        try {
            final var type = this.clazz;
            final var field = type.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findStaticVarHandle(type, name, clazz);
            final var classToUse = (Class<R>) wrapper(clazz);
            return () -> classToUse.cast(varHandle.get());
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    /**
     * Creates a new consumer which can be used to set the value of
     * the static field for the current type. The generic
     * type will be the same as the given class type.
     * @param name The name of the field
     * @param clazz The type for the field
     * @param <R> The generic type
     * @return A new Consumer
     */
    @SuppressWarnings("unchecked")
    public <R> Consumer<R> createStaticSetter(String name, Class<R> clazz) {
        try {
            final var type = this.clazz;
            final var field = type.getDeclaredField(name);
            final var modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final var varHandle = lookupToUse.findStaticVarHandle(type, name, clazz);
            return value -> varHandle.set((R)value);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
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
    public ClassMirror<T> runStatic(String name, Object... args) {
        try {
            runOrCallMethod(void.class, name, args);
            return this;
        } catch (Throwable throwable) {
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
    public <R> ObjectMirror<R> callStatic(Class<R> clazz, String name, Object... args) {
        try {
            final var result = runOrCallMethod(clazz, name, args);
            Objects.requireNonNull(result, "The value returned from the method call is null!");
            return new ObjectMirror<>(clazz.cast(result), null, lookup);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    private Object runOrCallMethod(Class<?> returnType, String name, Object... args) throws Throwable {
        final var methodHandle = findMethod(returnType, name, args);
        return methodHandle.invokeWithArguments(args);
    }

    private MethodHandle findMethod(Class<?> returnType, String name, Object[] args) throws Throwable {
        final var types = types(args);
        final var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
        try {
            return privateLookup.findStatic(clazz, name, MethodType.methodType(returnType, types));
        } catch (NoSuchMethodException e) {
            try {
                return similarMethod(lookup, name, types);
            } catch (NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    /**
     * First public methods are searched in the class hierarchy, then private
     * methods on the declaring class. If a method could be found, it is
     * returned, otherwise a {@code NoSuchMethodException} is thrown.
     */
    private MethodHandle similarMethod(Lookup lookup, String name, Class<?>[] types) throws NoSuchMethodException, IllegalAccessException {
        // first priority: find a public method with a "similar" signature in class hierarchy
        // similar interpreted in when primitive argument types are converted to their wrappers
        for (var method : clazz.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return lookup.unreflect(method);
            }
        }
        // second priority: find a non-public method with a "similar" signature on declaring class
        for (var method : clazz.getDeclaredMethods()) {
            if (isSimilarSignature(method, name, types)) {
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
    public boolean equals(Object obj) {
        return obj instanceof ClassMirror && clazz.equals(((ClassMirror) obj).clazz);
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
