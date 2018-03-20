package com.github.elopteryx.reflect.tests;

import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AsTypeTest {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @SuppressWarnings("unused")
    private interface Parent {

        default int call() {
            return 0;
        }
    }

    @SuppressWarnings("unused")
    private static class Child implements Parent {

        @Override
        public int call() {
            return 1;
        }
    }

    @SuppressWarnings("unused")
    private static class GrandChild extends Child {

        @Override
        public int call() {
            return 2;
        }
    }

    @Test
    void asTypeWithInterface() {
        final var childValue = BeanMirror.of(new Child(), lookup).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new Child(), lookup).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                () -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue));
    }

    @Test
    void asTypeWithThreeLevels() {
        final var grandChildValue = BeanMirror.of(new GrandChild(), lookup).call(int.class, "call").get();
        final var childValue = BeanMirror.of(new GrandChild(), lookup).asType(Child.class).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new GrandChild(), lookup).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                //() -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue),
                () -> assertEquals(2, (int)grandChildValue));
    }

}
