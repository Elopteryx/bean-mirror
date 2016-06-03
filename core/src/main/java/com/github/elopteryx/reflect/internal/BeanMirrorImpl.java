package com.github.elopteryx.reflect.internal;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Mirror implementation which uses plain reflection to handle the given objects.
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BeanMirrorImpl<T> implements BeanMirror<T> {

    public static final BeanMirrorImpl EMPTY = new BeanMirrorImpl();

    /**
     * The wrapped object
     */
    private final T object;

    /**
     * The wrapped class
     */
    private final Class<T> clazz;

    /**
     * Field used for retrieving info about field values.
     * Only used if the Mirror was created with the methods {@link #field(String)}} or
     * {@link #field(String, Class)}.
     */
    private final Field field;

    /**
     * Method used for retrieving info about method return values.
     * Only used if the Mirror was created with the methods {@link #method(String, Object...)} or
     * {@link #method(String, Class, Object...)}.
     */
    private final Method method;

    private final Exception exception;

    private BeanMirrorImpl() {
        this.object = null;
        this.clazz = null;
        this.field = null;
        this.method = null;
        this.exception = null;
    }

    public BeanMirrorImpl(T object) {
        this.object = Objects.requireNonNull(object);
        this.clazz = null;
        this.field = null;
        this.method = null;
        this.exception = null;
    }

    public BeanMirrorImpl(Class<T> clazz) {
        this.object = null;
        this.clazz = Objects.requireNonNull(clazz);
        this.field = null;
        this.method = null;
        this.exception = null;
    }

    public BeanMirrorImpl(BeanMirrorImpl other, Exception exception) {
        this.object = (T)other.object;
        this.clazz = other.clazz;
        this.field = other.field;
        this.method = other.method;
        this.exception = exception;
    }

    /**
     * Private constructor which is only used if the Mirror is switched
     * to a field.
     * @param object The value of the field, can be null
     * @param field The field instance
     */
    private BeanMirrorImpl(T object, Field field, Exception exception) {
        this.object = object;
        this.clazz = null;
        this.field = field;
        this.method = null;
        this.exception = exception;
    }

    /**
     * Private constructor which is only used if the Mirror is switched
     * to the result of a method invocation.
     * @param object The value of the method invocation, can be null
     * @param method The invoked method
     */
    private BeanMirrorImpl(T object, Method method, Exception exception) {
        this.object = object;
        this.clazz = null;
        this.field = null;
        this.method = method;
        this.exception = exception;
    }

    private static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new BeanMirrorException(e);
            }
        };
    }

    /**
     * Call a constructor.
     * <p>
     * This is roughly equivalent to {@link Constructor#newInstance(Object...)}.
     * If the wrapped object is a {@link Class}, then this will construct a new
     * object of that class. If the wrapped object is any other {@link Object},
     * then this will construct a new object of the same type.
     * <p>
     * Just like {@link Constructor#newInstance(Object...)}, this will try to
     * wrap primitive types or unwrap primitive type wrappers if applicable. If
     * several constructors are applicable, by that rule, the first one
     * encountered is called. i.e. when calling <code><pre>
     * on(C.class).construct(1, 1);
     * </pre></code> The first of the following constructors will be applied:
     * <code><pre>
     * public C(int param1, Integer param2);
     * public C(Integer param1, int param2);
     * public C(Number param1, Number param2);
     * public C(Number param1, Object param2);
     * public C(int param1, Object param2);
     * </pre></code>
     *
     * @param args The constructor arguments
     * @return The wrapped new object, to be used for further reflection.
     * @throws BeanMirrorException If any reflection exception occurred.
     */
    @Override
    public BeanMirror<T> construct(Object... args) throws BeanMirrorException {
        if (isFailure()) {
            return this;
        }
        Class<T> type = type();
        Class<?>[] types = types(args);
        try {
            // Try invoking the "canonical" constructor, i.e. the one with exact
            // matching argument types
            return Try.of(() -> type.getDeclaredConstructor(types))
                    .map(BeanMirrorImpl::accessible)
                    .map(constructor -> constructor.newInstance(args))
                    .map(BeanMirror::of)
                    .orElseThrow();
        } catch (NoSuchMethodException e) {
            // If there is no exact match, try to find one that has a "similar"
            // signature if primitive argument types are converted to their wrappers
            return Arrays.stream(type().getDeclaredConstructors())
                    .filter(constructor -> match(constructor.getParameterTypes(), types))
                    .map(BeanMirrorImpl::accessible)
                    .map(unchecked(constructor -> constructor.newInstance(args)))
                    .map(type::cast)
                    .map(BeanMirror::of)
                    .findFirst()
                    .orElse(new BeanMirrorImpl<>(this, e));
        } catch (Exception e) {
            return new BeanMirrorImpl<>(this, e);
        }
    }

    @Override
    public BeanMirror<T> set(String fieldName, Object value) {
        if (isFailure()) {
            return this;
        }
        Try.of(() -> retrieveField(fieldName))
                .map(foundField -> { foundField.set(object, value); return foundField;})
                .orElseThrow(BeanMirrorException::new);
        return this;
    }

    @Override
    public Optional<T> value() {
        // TODO throw???
        return Optional.ofNullable(object);
    }

    @Override
    public Optional<Object> value(String fieldName) {
        return field(fieldName).value();
    }

    @Override
    public <R> Optional<R> value(String fieldName, Class<R> fieldClass) {
        return field(fieldName).value().map(fieldClass::cast);
    }

    @Override
    public Class<T> type() {
        if (object != null) {
            return (Class<T>) object.getClass();
        } else if (clazz != null) {
            return clazz;
        } else if (field != null) {
            return (Class<T>) field.getType();
        } else if (method != null) {
            return (Class<T>) method.getReturnType();
        } else {
            throw new BeanMirrorException(exception);
        }
    }

    @Override
    public BeanMirror<Object> field(String fieldName) {
        return fieldImpl(fieldName, Object.class);
    }

    @Override
    public <R> BeanMirror<R> field(String fieldName, Class<R> fieldClass) {
        return fieldImpl(fieldName, fieldClass);
    }

    private <R> BeanMirrorImpl<R> fieldImpl(String fieldName, Class<R> type) {
        if (isFailure()) {
            return (BeanMirrorImpl<R>)this;
        }
        if (object != null) {
            return Try.of(() -> retrieveField(fieldName))
                    .map(field -> field.get(object))
                    .map(type::cast)
                    .map(value -> new BeanMirrorImpl<>(value, field, null))
                    .orElse(e -> new BeanMirrorImpl<>((BeanMirrorImpl<R>)this, e));
        } else if (clazz != null) {
            return Try.of(() -> type.getDeclaredField(fieldName))
                    .map(field -> field.get(null))
                    .map(type::cast)
                    .map(value -> new BeanMirrorImpl<>(value, field, null))
                    .orElse(e -> new BeanMirrorImpl<>((BeanMirrorImpl<R>)this, e));
        } else {
            throw new IllegalStateException("The wrapped object is null!");
        }
    }

    private Field retrieveField(String name) throws Exception {
        Class<?> type = type();
        try {
            return type.getField(name);
        } catch (NoSuchFieldException e) {
            do {
                try {
                    Field field = type.getDeclaredField(name);
                    accessible(field);
                    return field;
                } catch (NoSuchFieldException ignore) {}
                type = type.getSuperclass();
            } while (type != null);
            throw e;
        }
    }

    /**
     * Call a method by its name.
     * <p>
     * This is roughly equivalent to {@link Method#invoke(Object, Object...)}.
     * If the wrapped object is a {@link Class}, then this will invoke a static
     * method. If the wrapped object is any other {@link Object}, then this will
     * invoke an instance method.
     * <p>
     * Just like {@link Method#invoke(Object, Object...)}, this will try to wrap
     * primitive types or unwrap primitive type wrappers if applicable. If
     * several methods are applicable, by that rule, the first one encountered
     * is called. i.e. when calling <code><pre>
     * on(...).call("method", 1, 1);
     * </pre></code> The first of the following methods will be called:
     * <code><pre>
     * public void method(int param1, Integer param2);
     * public void method(Integer param1, int param2);
     * public void method(Number param1, Number param2);
     * public void method(Number param1, Object param2);
     * public void method(int param1, Object param2);
     * </pre></code>
     * <p>
     * The best matching method is searched for with the following strategy:
     * <ol>
     * <li>public method with exact signature match in class hierarchy</li>
     * <li>non-public method with exact signature match on declaring class</li>
     * <li>public method with similar signature in class hierarchy</li>
     * <li>non-public method with similar signature on declaring class</li>
     * </ol>
     *
     * @param methodName The method name
     * @param args The method arguments
     * @return The wrapped method result or the same wrapped object if the
     *         method returns <code>void</code>, to be used for further
     *         reflection.
     * @throws BeanMirrorException If any reflection exception occurred.
     */
    @Override
    public BeanMirror<T> invoke(String methodName, Object... args) {
        if (isFailure()) {
            return this;
        }
        return Try.of(() -> findCorrectMethod(methodName, (Object[]) args))
                .map(BeanMirrorImpl::accessible)
                .map(method -> method.invoke(object, args))
                .map(param -> this)
                .orElse(e -> new BeanMirrorImpl<>(this, e));
    }

    @Override
    public BeanMirror<Object> method(String methodName, Object... args) {
        return methodImpl(methodName, Object.class, args);
    }

    @Override
    public <R> BeanMirror<R> method(String methodName, Class<R> methodReturnValueClass, Object... args) {
        return methodImpl(methodName, methodReturnValueClass, args);
    }

    /**
     * Wrap an object returned from a method
     */
    private <R> BeanMirror<R> methodImpl(String methodName, Class<R> clazz, Object... args) {
        if (isFailure()) {
            return (BeanMirror<R>)this;
        }
        return Try.of(() -> findCorrectMethod(methodName, args))
                .map(BeanMirrorImpl::accessible)
                .map(method -> method.invoke(object, args))
                .map(clazz::cast)
                .map(result -> new BeanMirrorImpl<>(result, method, null))
                .orElse(e -> new BeanMirrorImpl<>((BeanMirrorImpl<R>)this, e));
    }

    private Method findCorrectMethod(String methodName, Object... args) {
        Class<?>[] types = types(args);

        // Try invoking the "canonical" method, i.e. the one with exact
        // matching argument types
        try {
            return exactMethod(methodName, types);
        } catch (NoSuchMethodException e) {
            // If there is no exact match, try to find a method that has a "similar"
            // signature if primitive argument types are converted to their wrappers
            try {
                return similarMethod(methodName, types);
            } catch (NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    /**
     * Get an array of types for an array of objects
     *
     * @see Object#getClass()
     */
    private static Class<?>[] types(Object... values) {
        if (values == null) {
            return new Class[0];
        }
        return Arrays.stream(values)
                .map(value -> value == null ? None.class : value.getClass())
                .toArray(Class[]::new);
    }

    /**
     * Searches a method with the exact same signature as desired.
     * <p>
     * If a public method is found in the class hierarchy, this method is returned.
     * Otherwise a private method with the exact same signature is returned.
     * If no exact match could be found, we let the {@code NoSuchMethodException} pass through.
     */
    private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        try {
            // first priority: find a public method with exact signature match in class hierarchy
            return type.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            // second priority: find a private method with exact signature match on declaring class
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignore) {}

                type = type.getSuperclass();
            } while (type != null);

            throw new NoSuchMethodException();
        }
    }

    /**
     * Searches a method with a similar signature as desired using
     * {@link #isSimilarSignature(java.lang.reflect.Method, String, Class[])}.
     * <p>
     * First public methods are searched in the class hierarchy, then private
     * methods on the declaring class. If a method could be found, it is
     * returned, otherwise a {@code NoSuchMethodException} is thrown.
     */
    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        // first priority: find a public method with a "similar" signature in class hierarchy
        // similar interpreted in when primitive argument types are converted to their wrappers
        for (Method method : type.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return method;
            }
        }

        // second priority: find a non-public method with a "similar" signature on declaring class
        do {
            for (Method method : type.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    return method;
                }
            }
            type = type.getSuperclass();
        } while (type != null);

        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    /**
     * Determines if a method has a "similar" signature, especially if wrapping
     * primitive argument types would result in an exactly matching signature.
     */
    private boolean isSimilarSignature(Method possiblyMatchingMethod, String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName) && match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    /**
     * Check whether two arrays of types match, converting primitive types to
     * their corresponding wrappers.
     */
    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            return !IntStream.range(0, actualTypes.length)
                    .filter(i -> actualTypes[i] != None.class)
                    .filter(i -> !wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i])))
                    .findFirst()
                    .isPresent();

            // TODO Too much negation

//            for (int i = 0; i < actualTypes.length; i++) {
//                if (actualTypes[i] == None.class) {
//                    continue;
//                }
//                if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
//                    continue;
//                }
//                return false;
//            }
//            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a wrapper type for a primitive type, or the argument type itself, if
     * it is not a primitive type.
     */
    private static Class<?> wrapper(Class<?> type) {
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

    /**
     * Conveniently render an {@link AccessibleObject} accessible.
     * <p>
     * To prevent {@link SecurityException}, this is only done if the argument
     * object and its declaring class are non-public.
     *
     * @param accessible The object to render accessible
     */
    private static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) &&
                    Modifier.isPublic(member.getDeclaringClass().getModifiers())) {

                return accessible;
            }
        }

        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public boolean isFailure() {
        return exception != null;
    }

    @Override
    public T orElse(T other) {
        if (isSuccess()) {
            return object;
        } else {
            return other;
        }
    }

    @Override
    public T orElseThrow() {
        if (isSuccess()) {
            return object;
        } else {
            throw new BeanMirrorException(exception);
        }
    }

    @Override
    public <X extends Exception> T orElseThrow(Function<Exception, ? extends X> function) throws X {
        if (isSuccess()) {
            return object;
        } else {
            throw function.apply(exception);
        }
    }

    private static class None {}
}
