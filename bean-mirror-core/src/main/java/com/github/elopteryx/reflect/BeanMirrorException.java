package com.github.elopteryx.reflect;

/**
 * Custom RuntimeException class, to wrap the exceptions
 * caused by the invalid invocations.
 */
public class BeanMirrorException extends RuntimeException {

    /**
     * Default constructor.
     * @param cause The wrapped error
     */
    public BeanMirrorException(final Throwable cause) {
        super(cause);
    }

}
