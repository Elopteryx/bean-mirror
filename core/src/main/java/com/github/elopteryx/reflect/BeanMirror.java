package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.internal.BeanMirrorImpl;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A wrapper object created from an {@link Object} or {@link Class} instance.
 * @param <T> The generic type of the wrapped object.
 */
public interface BeanMirror<T> {

    /**
     * Creates a new Mirror instance from the given object.
     * @param object The object to be wrapped
     * @param <R> The type of the object
     * @throws NullPointerException If the object is null
     * @return A new Mirror instance
     */
    static <R> BeanMirror<R> of(R object) {
        try {
            return new BeanMirrorImpl<>(Objects.requireNonNull(object));
        } catch (Exception e) {
            return new BeanMirrorImpl<>(BeanMirrorImpl.EMPTY, e);
        }
    }

    /**
     * Creates a new Mirror instance from the given object.
     * @param clazz The class object to be wrapped
     * @param <R> The type of the object
     * @throws NullPointerException If the object is null
     * @return A new Mirror instance
     */
    static <R> BeanMirror<R> of(Class<R> clazz) {
        try {
            return new BeanMirrorImpl<>(Objects.requireNonNull(clazz));
        } catch (Exception e) {
            return new BeanMirrorImpl<>(BeanMirrorImpl.EMPTY, e);
        }
    }

    static BeanMirror<Object> of(String className) {
        try {
            return new BeanMirrorImpl<>((Class<Object>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            return new BeanMirrorImpl<>(BeanMirrorImpl.EMPTY, e);
        }
    }

    // Constructors

    /**
     * Creates a new instance from the wrapped object or class instance.
     * @param args The constructor arguments to be used
     * @return A new Mirror instance wrapping the newly created object
     */
    BeanMirror<T> construct(Object... args);


    // Current value

    /**
     * Returns the currently wrapped value as an optional. The given value can be missing
     * if the Mirror was created from a field value or a method invocation.
     * @return An optional, possibly empty
     */
    Optional<T> value();

    /**
     * Returns the value of the field identified with the given name as an optional. Because
     * of the missing type information the generic type is widened to Object.
     * @param fieldName The name of the field
     * @return An optional, possibly empty if the field has null value
     */
    Optional<Object> value(String fieldName);

    /**
     * Returns the value of the field identified with the given name as an optional.
     * The Optional will get the type information from the given class instance.
     * @param fieldName The name of the field
     * @param fieldClass The known class of the field
     * @throws ClassCastException If the classes do not match
     * @return An optional, possibly empty if the field has null value
     */
    <R> Optional<R> value(String fieldName, Class<R> fieldClass);

    /**
     * Returns the class of the currently wrapped instance.
     * @return The available type information
     */
    Class<T> type();


    // Fields

    BeanMirror<T> set(String fieldName, Object value);

    BeanMirror<Object> field(String fieldName);

    <R> BeanMirror<R> field(String fieldName, Class<R> fieldClass);



    // Methods

    BeanMirror<T> invoke(String methodName, Object... args);

    BeanMirror<Object> method(String methodName, Object... args);

    <R> BeanMirror<R> method(String methodName, Class<R> methodReturnValueClass, Object... args);


    // State

    boolean isSuccess();

    boolean isFailure();

    T orElse(T other);

    T orElseThrow() throws Exception;

    <X extends Exception> T orElseThrow(Function<Exception, ? extends X> function) throws X;


}
