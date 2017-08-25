package com.github.elopteryx.reflect.internal;

import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AccessorImpl implements Accessor {

    @SuppressWarnings("unused")
    AccessorImpl(Lookup lookup) {
        // Unused, only for compatibility.
    }

    // CONSTRUCTORS

    @Override
    public Object useConstructor(Class<?> clazz, Object... args) {
        final Class<?>[] types = types(args);

        // Try invoking the "canonical" constructor, i.e. the one with exact
        // matching argument types
        try {
            final Constructor<?> constructor = clazz.getDeclaredConstructor(types);
            return useConstructorInternal(constructor, args);
        }

        // If there is no exact match, try to find one that has a "similar"
        // signature if primitive argument types are converted to their wrappers
        catch (NoSuchMethodException e) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (match(constructor.getParameterTypes(), types)) {
                    return useConstructorInternal(constructor, args);
                }
            }

            throw new BeanMirrorException(e);
        }
    }

    /**
     * Wrap an object created from a constructor
     */
    private static Object useConstructorInternal(Constructor<?> constructor, Object... args) {
        try {
            return accessible(constructor).newInstance(args);
        } catch (Exception e) {
            throw new BeanMirrorException(e);
        }
    }

    // FIELDS

    @Override
    public Object getField(Object object, String name, Class<?> fieldType) {
        try {
            Field field = getFieldInternal(object, name);
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            return field.get(object);
        } catch (Exception e) {
            throw new BeanMirrorException(e);
        }
    }

    @Override
    public void setField(Object object, String name, Class<?> fieldType, Object value) {
        try {
            Field field = getFieldInternal(object, name);
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.set(object, value);
        } catch (Exception e) {
            throw new BeanMirrorException(e);
        }
    }

    private Field getFieldInternal(Object object, String name) throws BeanMirrorException {
        Class<?> type = type(object);

        // Try getting a public field
        try {
            return accessible(type.getField(name));
        } catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                }  catch (NoSuchFieldException ignore) {}

                type = type.getSuperclass();
            }
            while (type != null);

            throw new BeanMirrorException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Function<T, R> createGetter(Object target, String name, Class<R> clazz) {
        try {
            final Class<?> type = type(target);
            final Field field = type.getDeclaredField(name);
            final int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers) && !field.isAccessible()) {
                field.setAccessible(true);
            }
            final Class<R> classToUse = (Class<R>) Accessor.wrapper(clazz);
            return obj -> {
                try {
                    return classToUse.cast(field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new BeanMirrorException(e);
                }
            };
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Supplier<R> createStaticGetter(Class<T> target, String name, Class<R> clazz) {
        try {
            final Class<?> type = type(target);
            final Field field = type.getDeclaredField(name);
            final int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers) && !field.isAccessible()) {
                field.setAccessible(true);
            }
            final Class<R> classToUse = (Class<R>) Accessor.wrapper(clazz);
            return () -> {
                try {
                    return classToUse.cast(field.get(null));
                } catch (IllegalAccessException e) {
                    throw new BeanMirrorException(e);
                }
            };
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    @Override
    public <T, R> BiConsumer<T, R> createSetter(Object target, String name, Class<R> clazz) {
        try {
            final Class<?> type = type(target);
            final Field field = type.getDeclaredField(name);
            final int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers) && !field.isAccessible()) {
                field.setAccessible(true);
            }
            return (obj, value) -> {
                try {
                    field.set(obj, value);
                } catch (IllegalAccessException e) {
                    throw new BeanMirrorException(e);
                }
            };
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    @Override
    public <T, R> Consumer<R> createStaticSetter(Class<T> target, String name, Class<R> clazz) {
        try {
            final Class<?> type = type(target);
            final Field field = type.getDeclaredField(name);
            final int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers) && !field.isAccessible()) {
                field.setAccessible(true);
            }
            return value -> {
                try {
                    field.set(null, value);
                } catch (IllegalAccessException e) {
                    throw new BeanMirrorException(e);
                }
            };
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    // METHODS

    @Override
    public void runMethod(Object object, String name, Object... args) throws BeanMirrorException {
        Class<?>[] types = types(args);

        // Try invoking the "canonical" method, i.e. the one with exact
        // matching argument types
        try {
            Method method = exactMethod(object, name, types);
            callMethodInternal(method, object, args);
        }

        // If there is no exact match, try to find a method that has a "similar"
        // signature if primitive argument types are converted to their wrappers
        catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(object, name, types);
                callMethodInternal(method, object, args);
            } catch (NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    @Override
    public Object callMethod(Object object, String name, Object... args) throws BeanMirrorException {
        Class<?>[] types = types(args);

        // Try invoking the "canonical" method, i.e. the one with exact
        // matching argument types
        try {
            Method method = exactMethod(object, name, types);
            return callMethodInternal(method, object, args);
        }

        // If there is no exact match, try to find a method that has a "similar"
        // signature if primitive argument types are converted to their wrappers
        catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(object, name, types);
                return callMethodInternal(method, object, args);
            } catch (NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
        }
    }

    private static Object callMethodInternal(Method method, Object object, Object... args) throws BeanMirrorException {
        try {
            accessible(method);

            if (method.getReturnType() == void.class) {
                method.invoke(object, args);
                return object;
            } else {
                return method.invoke(object, args);
            }
        }
        catch (Exception e) {
            throw new BeanMirrorException(e);
        }
    }

    /**
     * Searches a method with the exact same signature as desired.
     * <p>
     * If a public method is found in the class hierarchy, this method is returned.
     * Otherwise a private method with the exact same signature is returned.
     * If no exact match could be found, we let the {@code NoSuchMethodException} pass through.
     */
    private Method exactMethod(Object object, String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type(object);

        // first priority: find a public method with exact signature match in class hierarchy
        try {
            return type.getMethod(name, types);
        }

        // second priority: find a private method with exact signature match on declaring class
        catch (NoSuchMethodException e) {
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                }
                catch (NoSuchMethodException ignore) {}

                type = type.getSuperclass();
            }
            while (type != null);

            throw e;
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
    private Method similarMethod(Object object, String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type(object);

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
        }
        while (type != null);

        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type(object) + ".");
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
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class)
                    continue;

                if (Accessor.wrapper(declaredTypes[i]).isAssignableFrom(Accessor.wrapper(actualTypes[i])))
                    continue;

                return false;
            }

            return true;
        }
        else {
            return false;
        }
    }

    // MISC

    private Class<?> type(Object object) {
        return object instanceof Class ? (Class<?>) object : object.getClass();
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

        Class<?>[] result = new Class[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    private static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                return accessible;
            }
        }

        // [jOOQ #3392] The accessible flag is set to false by default, also for public members.
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    private static class NULL {}

}
