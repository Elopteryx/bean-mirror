package com.github.elopteryx.reflect.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.tests.astype.Child;
import com.github.elopteryx.reflect.tests.astype.GrandChild;
import com.github.elopteryx.reflect.tests.astype.Parent;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

class AsTypeTest {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

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
