package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.internal.Accessor;

import java.lang.invoke.MethodHandles.Lookup;
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
     * The accessor used to hack into the
     * properties of the value. Uses the
     * lookup object which must be supplied
     * from the client code.
     */
    private final Accessor accessor;

    private BeanMirror(T object, Accessor accessor) {
        this.object = object;
        this.accessor = accessor;
        this.superType = null;
    }

    private BeanMirror(T object, Class<? super T> superType, Accessor accessor) {
        this.object = object;
        this.superType = superType;
        this.accessor = accessor;
    }

    private BeanMirror(Class<T> object, Accessor accessor, Class<? super T> superType) {
        this.object = object;
        this.superType = superType;
        this.accessor = accessor;
    }

    public static <R> BeanMirror<R> of(R object, Lookup lookup) {
        Objects.requireNonNull(object);
        return new BeanMirror<>(object, null, Accessor.of(lookup, null));
    }

    public static <R> BeanMirror<R> of(Class<R> clazz, Lookup lookup) {
        Objects.requireNonNull(clazz);
        return new BeanMirror<>(clazz, Accessor.of(lookup, null), null);
    }

    // TYPE

    /**
     * Performs an up cast, allowing to treat the object as one of its
     * ancestor types. This allows field and method lookup from the given
     * super type. Useful if the child type has redefined ('shadowed') a
     * property with the same name.
     * @param clazz The type to be used
     * @return A new mirror instance, using the given type
     */
    @SuppressWarnings("unchecked")
    public <R> BeanMirror<R> asType(Class<R> clazz) {
        if (object instanceof Class) {
            if (clazz.isAssignableFrom((Class)object)) {
                return new BeanMirror<>((Class<R>)object, Accessor.of(accessor.getLookup(), clazz), clazz);
            } else {
                throw new IllegalArgumentException("Not a supertype!");
            }
        } else {
            if (clazz.isAssignableFrom(object.getClass())) {
                return new BeanMirror<>((R)object, clazz, Accessor.of(accessor.getLookup(), clazz));
            } else {
                throw new IllegalArgumentException("Not a supertype!");
            }
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
        return object instanceof Class ? (Class<?>)object : object.getClass();
    }

    // VALUE

    /**
     * Returns the current value.
     * @return The current value
     */
    @SuppressWarnings("unchecked")
    public T get() {
        return (T)object;
    }

    // CONSTRUCTORS

    /**
     * Creates a new instance from the current type. Returns it
     * wrapped into the mirror.
     * @param args The constructor arguments
     * @return A new mirror instance, wrapping the created object
     */
    @SuppressWarnings("unchecked")
    public BeanMirror<T> create(Object... args)  {
        final T result;
        try {
            result = (T)accessor.useConstructor(type(), args);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
        return new BeanMirror<>(result, accessor);
    }

    // FIELDS

    /**
     * Gets the value of the field, identified by its name.
     * @param name
     * @param clazz
     * @param <R>
     * @return
     */
    public <R> R get(String name, Class<R> clazz) {
        return field(name, clazz).get();
    }

    public BeanMirror<T> set(String name, Object value) {
        accessor.setField(object, name, value.getClass(), value);
        return this;
    }

    public <R> BeanMirror<R> field(String name, Class<R> clazz) {
        final var result = accessor.getField(object, name, clazz);
        Objects.requireNonNull(result, "Field: " + name);
        return new BeanMirror<>(clazz.cast(result), accessor);
    }

    public <R> Function<T, R> createGetter(String name, Class<R> clazz) {
        try {
            return accessor.createGetter(object, name, clazz);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> Supplier<R> createStaticGetter(String name, Class<R> clazz) {
        try {
            return accessor.createStaticGetter(type(), name, clazz);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> BiConsumer<T, R> createSetter(String name, Class<R> clazz) {
        try {
            return accessor.createSetter(object, name, clazz);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> Consumer<R> createStaticSetter(String name, Class<R> clazz) {
        try {
            return accessor.createStaticSetter(type(), name, clazz);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    // METHODS

    public BeanMirror<T> run(String name, Object... args) {
        accessor.callMethod(object, name, args);
        return this;
    }

    public <R> BeanMirror<R> call(Class<R> clazz, String name, Object... args) {
        final var result = accessor.callMethod(object, name, args);
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
