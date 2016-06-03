package com.github.elopteryx.reflect;

import java.lang.reflect.InvocationTargetException;

/**
 * A unchecked wrapper for any of Java's checked reflection exceptions:
 * <p>
 * These exceptions are
 * <ul>
 * <li> {@link ClassNotFoundException}</li>
 * <li> {@link IllegalAccessException}</li>
 * <li> {@link IllegalArgumentException}</li>
 * <li> {@link InstantiationException}</li>
 * <li> {@link InvocationTargetException}</li>
 * <li> {@link NoSuchMethodException}</li>
 * <li> {@link NoSuchFieldException}</li>
 * <li> {@link SecurityException}</li>
 * </ul>
 *
 * @author Lukas Eder
 */
public class BeanMirrorException extends RuntimeException {

    public BeanMirrorException() {
        super();
    }

    public BeanMirrorException(Throwable cause) {
        super(cause);
    }
}
