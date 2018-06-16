package com.github.elopteryx.reflect.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.BeanMirrorException;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

class BeanMirrorTest {

    @Test
    void createBeanMirror() {
        final var exception = assertThrows(BeanMirrorException.class,
                () -> BeanMirror.of(BeanMirror.class, MethodHandles.lookup()).create());
        assertEquals(UnsupportedOperationException.class, exception.getCause().getCause().getClass());
    }

    @Test
    void createNewObjectMirror() {
        final var object = new Object();
        final var lookup = MethodHandles.lookup();
        final var mirror = BeanMirror.of(object, lookup);
        assertEquals(object, mirror.get());
    }

    @Test
    void createObjectMirrorWithNullParams() {
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Object)null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Object)null, MethodHandles.lookup()));
        assertThrows(NullPointerException.class, () -> BeanMirror.of(new Object(), null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Object)null, null));
    }

    @Test
    void createNewClassMirror() {
        final var lookup = MethodHandles.lookup();
        BeanMirror.of(Object.class, lookup);
    }

    @Test
    void createClassMirrorWithNullParams() {
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Class<?>)null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Class<?>)null, MethodHandles.lookup()));
        assertThrows(NullPointerException.class, () -> BeanMirror.of(Object.class, null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Class<?>)null, null));
    }
}
