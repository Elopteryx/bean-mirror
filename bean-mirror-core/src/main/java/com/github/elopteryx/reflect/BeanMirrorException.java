package com.github.elopteryx.reflect;

/**
 * Custom RuntimeException class, to wrap the exceptions
 * caused by the invalid invocations.
 */
public class BeanMirrorException extends RuntimeException {

    public BeanMirrorException(Throwable cause) {
        super(cause);
    }

}
