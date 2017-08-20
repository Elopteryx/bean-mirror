package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.internal.Accessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic wrapper class for accessing the given instance.
 * @param <T> The generic type for the current value.
 */
public class BeanMirror<T> {

    /**
     * The wrapped value. Cannot be null.
     */
    private final Object object;

    /**
     * The super type. If defined then it is used
     * instead of the actual wrapped value
     * for accessing fields and methods.
     */
    private final Class<? super T> superType;

    /**
     * The lookup  object used for access
     * checking by the JVM. It must be supplied
     * from the client code.
     */
    private final Lookup lookup;

    /**
     * The accessor used to hack into the
     * properties of the value.
     */
    private final Accessor accessor;

    private BeanMirror(T object, Lookup lookup) {
        this.object = object;
        this.accessor = null;
        this.superType = null;
        this.lookup = lookup;
    }

    private BeanMirror(T object, Class<? super T> superType, Lookup lookup) {
        this.object = object;
        this.superType = superType;
        this.lookup = lookup;
        this.accessor = null;
    }

    private BeanMirror(Class<T> object, Lookup lookup, Class<? super T> superType) {
        this.object = object;
        this.superType = superType;
        this.lookup = lookup;
        this.accessor = null;
    }

    public static <R> BeanMirror<R> of(R object, Lookup lookup) {
        Objects.requireNonNull(object);
        return new BeanMirror<>(object, null, lookup);
    }

    public static <R> BeanMirror<R> of(Class<R> clazz, Lookup lookup) {
        Objects.requireNonNull(clazz);
        return new BeanMirror<>(clazz, lookup, null);
    }

    // TYPE

    @SuppressWarnings("unchecked")
    public <R> BeanMirror<R> asType(Class<R> clazz) {
        if (object instanceof Class) {
            if (clazz.isAssignableFrom((Class)object)) {
                return new BeanMirror<>((Class<R>)object, lookup, clazz);
            } else {
                throw new IllegalArgumentException("Not a supertype!");
            }
        } else {
            if (clazz.isAssignableFrom(object.getClass())) {
                return new BeanMirror<>((R)object, clazz, lookup);
            } else {
                throw new IllegalArgumentException("Not a supertype!");
            }
        }
    }

    private Class<?> type() {
        if (superType != null) {
            return superType;
        }
        return object instanceof Class ? (Class<?>)object : object.getClass();
    }

    // VALUE

    public T get() {
        return (T)object;
    }

    // CONSTRUCTORS

    @SuppressWarnings("unchecked")
    public BeanMirror<T> create(Object... args)  {
        final T result;
        try {
            result = (T)lookup.findConstructor(type(), MethodType.methodType(void.class)).invoke();
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
        Objects.requireNonNull(result, "");
        return new BeanMirror<>(result, lookup);
    }

    // FIELDS

    public Object get(String name) {
        return field(name).get();
    }

    public <R> R get(String name, Class<R> clazz) {
        return field(name, clazz).get();
    }

    @SuppressWarnings("unchecked")
    public <R> Function<T, R> createGetter(String name, Class<R> clazz) {
        try {
            final Field field = type().getDeclaredField(name);
            final int modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type(), lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findVarHandle(type(), name, clazz);
            final Class<R> classToUse = (Class<R>)Accessor.wrapper(clazz);
            return obj -> classToUse.cast(varHandle.get(obj));
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    public <R> Supplier<R> createStaticGetter(String name, Class<R> clazz) {
        try {
            final Field field = type().getDeclaredField(name);
            final int modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type(), lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findStaticVarHandle(type(), name, clazz);
            final Class<R> classToUse = (Class<R>)Accessor.wrapper(clazz);
            return () -> classToUse.cast(varHandle.get());
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> BiConsumer<T, R> createSetter(String name, Class<R> clazz) {
        try {
            final Field field = type().getDeclaredField(name);
            final int modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type(), lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findVarHandle(type(), name, clazz);
            return varHandle::set;
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> Consumer<R> createStaticSetter(String name, Class<R> clazz) {
        try {
            final Field field = type().getDeclaredField(name);
            final int modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type(), lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findStaticVarHandle(type(), name, clazz);
            return varHandle::set;
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
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
        return new BeanMirror<>(clazz.cast(result), lookup);
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
        return new BeanMirror<>(clazz.cast(result), lookup);
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
