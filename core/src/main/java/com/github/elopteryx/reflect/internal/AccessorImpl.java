package com.github.elopteryx.reflect.internal;

import com.github.elopteryx.reflect.BeanMirrorException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

public class AccessorImpl implements Accessor {

    private final Lookup lookup;

    AccessorImpl(Lookup lookup) {
        this.lookup = lookup;
    }

    //CONSTRUCTORS

    @Override
    public Object useConstructor(Class<?> clazz, Object... args) {
        try {
            final Class<?>[] types = types(args);

            final Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

            final MethodHandle constructorHandle = privateLookup.findConstructor(clazz, methodType(void.class, types));
            return constructorHandle.invoke(args);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // FIELDS

    @Override
    public Object getField(Object object, String fieldName, Class<?> fieldType) {
        try {
            final Class<?> clazz = object.getClass();
            final Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final VarHandle varHandle = privateLookup.findVarHandle(clazz, fieldName, fieldType);
            return varHandle.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setField(Object object, String fieldName, Class<?> fieldType, Object value) {
        try {
            final Class<?> clazz = object.getClass();
            final Field field = clazz.getDeclaredField(fieldName);

            final Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final VarHandle handle = privateLookup.unreflectVarHandle(field);

            handle.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Function<T, R> createGetter(Object target, String name, Class<R> clazz) {
        try {
            final Class<?> type = type(target);
            final Field field = type.getDeclaredField(name);
            final int modifiers = field.getModifiers();
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findVarHandle(type, name, clazz);
            final Class<R> classToUse = (Class<R>) Accessor.wrapper(clazz);
            return obj -> classToUse.cast(varHandle.get(obj));
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
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findStaticVarHandle(type, name, clazz);
            final Class<R> classToUse = (Class<R>) Accessor.wrapper(clazz);
            return () -> classToUse.cast(varHandle.get());
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
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findVarHandle(type, name, clazz);
            return varHandle::set;
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
            final Lookup lookupToUse;
            if (Modifier.isPrivate(modifiers) && field.trySetAccessible()) {
                lookupToUse = MethodHandles.privateLookupIn(type, lookup);
            } else {
                lookupToUse = lookup;
            }
            final VarHandle varHandle = lookupToUse.findStaticVarHandle(type, name, clazz);
            return varHandle::set;
        } catch (Throwable throwable) {
            throw new BeanMirrorException(throwable);
        }
    }

    // METHODS

    @Override
    public void runMethod(Object object, String name, Object... args) throws BeanMirrorException {
        try {
            final Class<?> clazz = type(object);
            final Class<?>[] types = types(args);

            final Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final Method method = findMethod(object, name, types);
            final MethodHandle methodHandle = privateLookup.unreflect(method);

            final Object[] targetWithArgs = new Object[args.length + 1];
            targetWithArgs[0] = object;
            System.arraycopy(args, 0, targetWithArgs, 1, args.length);

            methodHandle.invokeWithArguments(targetWithArgs);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object callMethod(Object object, String name, Object... args) {
        try {
            final Class<?> clazz = type(object);
            final Class<?>[] types = types(args);

            final Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            final Method method = findMethod(object, name, types);
            final MethodHandle methodHandle = privateLookup.unreflect(method);

            final Object[] targetWithArgs = new Object[args.length + 1];
            targetWithArgs[0] = object;
            System.arraycopy(args, 0, targetWithArgs, 1, args.length);

            return methodHandle.invokeWithArguments(targetWithArgs);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Object object, String name, Class<?>[] types) {
        try {
            return exactMethod(object, name, types);
        } catch (NoSuchMethodException e) {
            try {
                return similarMethod(object, name, types);
            } catch (NoSuchMethodException e1) {
                throw new BeanMirrorException(e1);
            }
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

    private static <T extends AccessibleObject> T accessible(T accessible, Object object) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                return accessible;
            }
        }
        if (!accessible.canAccess(object)) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    private static class NULL {}

}
