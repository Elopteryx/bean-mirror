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

        private char c = 'c';

        @Override
        public int call() {
            return 1;
        }
    }

    @SuppressWarnings("unused")
    private static class GrandChild extends Child {

        private char c = 'g';

        @Override
        public int call() {
            return 2;
        }
    }

    @Test
    void asTypeFieldWithTwoLevels() {
        final var grandChildValue = BeanMirror.of(new GrandChild(), lookup).get("c", char.class);
        final var childValue = BeanMirror.of(new GrandChild(), lookup).asType(Child.class).get("c", char.class);

        assertAll(
                () -> assertEquals('g', (char)grandChildValue),
                () -> assertEquals('c', (char)childValue));
    }

    @Test
    void asTypeMethodWithTwoLevels() {
        final var childValue = BeanMirror.of(new Child(), lookup).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new Child(), lookup).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                () -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue));
    }

    @Test
    void asTypeMethodWithThreeLevels() {
        final var grandChildValue = BeanMirror.of(new GrandChild(), lookup).call(int.class, "call").get();
        final var childValue = BeanMirror.of(new GrandChild(), lookup).asType(Child.class).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new GrandChild(), lookup).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                () -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue),
                () -> assertEquals(2, (int)grandChildValue));
    }

}
