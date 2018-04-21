package com.github.elopteryx.reflect.internal;

import java.lang.reflect.Method;

public final class Utils {

    private Utils() {
        // No need to instantiate.
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if a method has a "similar" signature, especially if wrapping
     * primitive argument types would result in an exactly matching signature.
     * @param candidateMethod The method to be checked
     * @param methodName The user supplied name
     * @param actualTypes The parameter types
     * @return Whether the method matches the search
     */
    public static boolean isSimilarSignature(Method candidateMethod, String methodName, Class<?>[] actualTypes) {
        if (!candidateMethod.getName().equals(methodName)) {
            return false;
        }
        final var declaredTypes = candidateMethod.getParameterTypes();
        if (declaredTypes.length != actualTypes.length) {
            return false;
        }
        for (var i = 0; i < actualTypes.length; i++) {
            if (actualTypes[i] == NULL.class) {
                continue;
            }
            if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static Class<?>[] types(Object... values) {
        if (values == null) {
            return new Class[0];
        }

        final var result = new Class[values.length];

        for (var i = 0; i < values.length; i++) {
            final var value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    /**
     * Get a wrapper type for a primitive type, or the argument type itself, if
     * it is not a primitive type.
     * @param type The class type to be wrapped
     * @return The wrapped type, or null
     */
    public static Class<?> wrapper(Class<?> type) {
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
}
