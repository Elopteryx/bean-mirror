package com.github.elopteryx.reflect.tests.internal;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.BeanMirrorException;
import com.github.elopteryx.reflect.internal.Functional;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionalTest {

    @SuppressWarnings("unused")
    private static class Target {

        private int a;

        // private int b;

        public int c;

        private static int d;

        // private static int e;

        public static int f;

    }

    @Test
    void createFunctional() {
        final var exception = assertThrows(BeanMirrorException.class,
                () -> BeanMirror.of(Functional.class, MethodHandles.lookup()).create());
        assertEquals(UnsupportedOperationException.class, exception.getCause().getCause().getClass());
    }

    @Test
    void getterForNonExistentField() {
        Functional.createGetter("a", MethodHandles.lookup(), Target.class, int.class);
        assertThrows(BeanMirrorException.class, () -> Functional.createGetter("b", MethodHandles.lookup(), Target.class, int.class));
        Functional.createStaticGetter("d", MethodHandles.lookup(), Target.class, int.class);
        assertThrows(BeanMirrorException.class, () -> Functional.createStaticGetter("e", MethodHandles.lookup(), Target.class, int.class));
    }

    @Test
    void getterForPublicField() {
        Functional.createGetter("c", MethodHandles.lookup(), Target.class, int.class);
        Functional.createStaticGetter("f", MethodHandles.lookup(), Target.class, int.class);
    }

    @Test
    void setterForNonExistentField() {
        Functional.createSetter("a", MethodHandles.lookup(), Target.class, int.class);
        assertThrows(BeanMirrorException.class, () -> Functional.createSetter("b", MethodHandles.lookup(), Target.class, int.class));
        Functional.createStaticSetter("d", MethodHandles.lookup(), Target.class, int.class);
        assertThrows(BeanMirrorException.class, () -> Functional.createStaticSetter("e", MethodHandles.lookup(), Target.class, int.class));
    }

    @Test
    void setterForPublicField() {
        Functional.createSetter("c", MethodHandles.lookup(), Target.class, int.class);
        Functional.createStaticSetter("f", MethodHandles.lookup(), Target.class, int.class);
    }
}
