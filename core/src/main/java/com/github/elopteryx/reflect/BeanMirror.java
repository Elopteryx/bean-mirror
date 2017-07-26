package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.function.LongGetter;
import com.github.elopteryx.reflect.internal.Accessor;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongSupplier;

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
     * The accessor used to hack into the
     * properties of the value.
     */
    private final Accessor accessor;

    private BeanMirror(T object, Accessor accessor) {
        this.object = object;
        this.accessor = accessor;
    }

    public static <R> BeanMirror<R> of(R object) {
        Objects.requireNonNull(object);
        return new BeanMirror<>(object, new Accessor(null)); // FIXME What about own lookup?
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

    public <T> LongGetter<T> getterForLong1(String name, MethodHandles.Lookup lookup) {

        try {
            final Field f = object.getClass().getDeclaredField(name);
            if (!long.class.isAssignableFrom(f.getType()))
                throw new RuntimeException("Field is not of expected type");

            return (obj) -> {
                try {
                    return f.getLong(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchFieldException e) {
            throw new BeanMirrorException(e);
        }
    }

    public LongGetter getterForLong(String name, MethodHandles.Lookup lookup) {
        try {
            final Field f = object.getClass().getDeclaredField(name);
            MethodHandle get = lookup.findVirtual(Field.class, "getLong",
                    MethodType.methodType(long.class, Object.class));
            //lookup.unreflectVarHandle(f).get()
            MethodHandle factory = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    //get.type().changeReturnType(LongGetter.class),
                    MethodType.methodType(LongGetter.class, Field.class),
                    MethodType.methodType(long.class),
                    get,
                    MethodType.methodType(long.class)
            ).getTarget();
            factory = factory.bindTo(f);
            return (LongGetter) factory.invoke(object);
            //return (LongGetter) factory.invoke(f, Modifier.isStatic(f.getModifiers())? null: object);



            //final MethodHandle getterHandle = lookup.unreflectGetter(object.getClass().getDeclaredField(name));
            //final MethodHandle getterHandle = lookup.findGetter(object.getClass(), name, long.class);
            //return PrivateTargetLambdaWorking.getterLambdaLong(lookup, getterHandle);
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    public <R> Function<T, R> getter(String name) {
        try {
            final MethodHandle getterHandle = MethodHandles.lookup().unreflect(object.getClass().getDeclaredMethod("getValue"));
            return PrivateTargetLambdaWorking.getterLambda2(MethodHandles.lookup(), getterHandle);
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
