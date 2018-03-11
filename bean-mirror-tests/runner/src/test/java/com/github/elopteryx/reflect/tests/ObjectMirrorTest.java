package com.github.elopteryx.reflect.tests;

import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectMirrorTest {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @SuppressWarnings("unused")
    private static class Parent {
        private int i = 3;
    }

    @SuppressWarnings("unused")
    private static class Child extends Parent {
        private int i = 4;
    }

    @Test
    void asType() {
        final var childValue = BeanMirror.of(new Child(), lookup).get("i", int.class);
        final var parentValue = BeanMirror.of(new Child(), lookup).asType(Parent.class).get("i", int.class);

        assertAll(
                () -> assertEquals(3, (int)parentValue),
                () -> assertEquals(4, (int)childValue));
    }

    @Test
    void get() {
        assertNotNull(BeanMirror.of(new Child(), lookup).get());
    }

    @SuppressWarnings("unused")
    private static class GetField {
        private String a = "a";
    }

    @Test
    void getField() {
        assertNotNull(BeanMirror.of(new GetField(), lookup).get("a", String.class));
    }

    @SuppressWarnings("unused")
    private static class SetField {
        private String a = "a";
    }

    @Test
    void setField() {
        final var mirror = BeanMirror.of(new SetField(), lookup);
        final var mirrorAfterSet = mirror.set("a", "b");
        assertEquals(mirror, mirrorAfterSet);
    }

    @SuppressWarnings("unused")
    private static class FieldTarget {
        private String value = "field";
    }

    @Test
    void field() {
        final var mirror = BeanMirror.of(new FieldTarget(), lookup).field("value", String.class);
        assertEquals(mirror.get(), "field");
    }

    @SuppressWarnings("unused")
    private static class GetterSetterTarget {
        private String value;

        private GetterSetterTarget() {
            this("");
        }

        private GetterSetterTarget(String value) {
            this.value = value;
        }

        private String getValue() {
            return value;
        }
    }

    @Test
    void createGetter() {
        final var getter = BeanMirror.of(new GetterSetterTarget(), lookup).createGetter("value", String.class);
        assertAll(
                () -> assertEquals(getter.apply(new GetterSetterTarget()), ""),
                () -> assertEquals(getter.apply(new GetterSetterTarget("a")), "a"),
                () -> assertEquals(getter.apply(new GetterSetterTarget("b")), "b")
        );
    }

    @Test
    void createSetter() {
        final var target = new GetterSetterTarget();
        final var setter = BeanMirror.of(target, lookup).createSetter("value", String.class);
        assertAll(
                () -> assertEquals("", target.getValue()),
                () -> {
                    setter.accept(target, "a");
                    assertEquals("a", target.getValue());
                },
                () -> {
                    setter.accept(target, "b");
                    assertEquals("b", target.getValue());
                },
                () -> {
                    setter.accept(target, "c");
                    assertEquals("c", target.getValue());
                }
        );
    }

    @Test
    void createGetterAndSetter() {
        final var target = new GetterSetterTarget();
        final var mirror = BeanMirror.of(target, lookup);
        final var getter = mirror.createGetter("value", String.class);
        final var setter = mirror.createSetter("value", String.class);
        assertAll(
                () -> assertEquals("", target.getValue()),
                () -> {
                    setter.accept(target, "a");
                    assertEquals("a", target.getValue());
                    assertEquals("a", getter.apply(target));
                },
                () -> {
                    setter.accept(target, "b");
                    assertEquals("b", target.getValue());
                    assertEquals("b", getter.apply(target));
                },
                () -> {
                    setter.accept(target, "c");
                    assertEquals("c", target.getValue());
                    assertEquals("c", getter.apply(target));
                }
        );
    }

    @SuppressWarnings("unused")
    private static class RunTarget {

        public void run(String param) {
            Objects.requireNonNull(param);
        }
    }

    @Test
    void run() {
        final var mirror = BeanMirror.of(new RunTarget(), lookup);
        mirror.run("run", "arg");
        final var exception = assertThrows(RuntimeException.class, () -> mirror.run("run", (String) null));
        assertEquals(exception.getCause().getClass(), NullPointerException.class);
    }

    @SuppressWarnings("unused")
    private static class CallTarget {

        private String call() {
            return "callable";
        }
    }

    @Test
    void call() {
        final var mirror = BeanMirror.of(new CallTarget(), lookup);
        assertEquals(mirror.call(String.class, "call").get(), "callable");
    }
}