package com.github.elopteryx.reflect.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.tests.astype.Child;
import com.github.elopteryx.reflect.tests.astype.GrandChild;
import com.github.elopteryx.reflect.tests.astype.Parent;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

class AsTypeTest {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Test
    void asTypeFieldWithTwoLevels() {
        final var grandChildValue = BeanMirror.of(new GrandChild(), LOOKUP).get("c", char.class);
        final var childValue = BeanMirror.of(new GrandChild(), LOOKUP).asType(Child.class).get("c", char.class);

        assertAll(
                () -> assertEquals('g', (char)grandChildValue),
                () -> assertEquals('c', (char)childValue));
    }

    @Test
    void asTypeMethodWithTwoLevels() {
        final var childValue = BeanMirror.of(new Child(), LOOKUP).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new Child(), LOOKUP).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                () -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue));
    }

    @Test
    void asTypeMethodWithThreeLevels() {
        final var grandChildValue = BeanMirror.of(new GrandChild(), LOOKUP).call(int.class, "call").get();
        final var childValue = BeanMirror.of(new GrandChild(), LOOKUP).asType(Child.class).call(int.class, "call").get();
        final var parentValue = BeanMirror.of(new GrandChild(), LOOKUP).asType(Parent.class).call(int.class, "call").get();

        assertAll(
                () -> assertEquals(0, (int)parentValue),
                () -> assertEquals(1, (int)childValue),
                () -> assertEquals(2, (int)grandChildValue));
    }

    @Test
    void invalidType() {
        assertThrows(IllegalArgumentException.class, () -> BeanMirror.of(new Exception()).asType(Error.class));
    }

}
