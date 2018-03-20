package com.github.elopteryx.reflect.tests.internal;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.BeanMirrorException;
import com.github.elopteryx.reflect.internal.NULL;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NULLTest {

    @Test
    void createNULL() {
        final var exception = assertThrows(BeanMirrorException.class,
                () -> BeanMirror.of(NULL.class, MethodHandles.lookup()).create());
        assertEquals(UnsupportedOperationException.class, exception.getCause().getCause().getClass());
    }
}
