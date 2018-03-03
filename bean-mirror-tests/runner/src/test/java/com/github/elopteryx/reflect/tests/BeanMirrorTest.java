package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanMirrorTest {

    @Test
    void legalAccess() throws Exception {
        System.out.println("Running legalAccess");
        Client.legalAccess();
    }

    @Test
    void unauthorizedAccess() {
        System.out.println("Running unauthorizedAccess");
        // final var exception = assertThrows(RuntimeException.class, Client::unauthorizedAccess);
    }

    public static class Base {
        protected String a = "a";
    }

    public static class Target extends Base {
        private static Long value = 3L;
        protected char b = 'b';
    }

    public static class Child extends Target {
        protected String a = "shadowed_a";
        protected char c = 'c';

        public void run(String param) {
            Objects.requireNonNull(param);
        }

        public String call() {
            return "callable";
        }
    }

    @Test
    void tryThings() {

        final var target = new Target();
        final var lookup = MethodHandles.lookup();
        final var beanMirror = BeanMirror.of(target, lookup);
        final var getter = beanMirror.createGetter("b", char.class);
        assertEquals((char)getter.apply(target), 'b');

        final var baseMirror = beanMirror.asType(Base.class);
        baseMirror.set("a", "changed a");
        assertEquals(baseMirror.get("a", String.class), "changed a");
        baseMirror.set("a", "a");
        assertEquals(baseMirror.get("a", String.class), "a");

        final var baseGetter = baseMirror.createGetter("a", String.class);
        baseMirror.set("a", "changed a");
        assertEquals(baseGetter.apply(target), "changed a");
        baseMirror.set("a", "a");
        assertEquals(baseGetter.apply(target), "a");


        final var child = new Child();

        final var castedMirror = BeanMirror.of(child, lookup);
        assertEquals(castedMirror.createGetter("a", String.class).apply(child), "shadowed_a");
        assertEquals(castedMirror.asType(Base.class).createGetter("a", String.class).apply(child), "a");
        assertEquals((char)castedMirror.asType(Child.class).createGetter("c", char.class).apply(child), 'c');

        final var childMirror = BeanMirror.of(Child.class, lookup).create();
        final var newChild = childMirror.get();
        assertEquals(newChild.getClass(), Child.class);

        childMirror.run("run", "arg");
        final var exception = assertThrows(RuntimeException.class, () -> childMirror.run("run", (String) null));
        assertEquals(exception.getCause().getClass(), NullPointerException.class);

        assertEquals(childMirror.call(String.class, "call").get(), "callable");

    }

    @Test
    void setField() {
        final var lookup = MethodHandles.lookup();
        final var mirror = BeanMirror.of(Base.class, lookup).create();

        final var mirrorAfterSet = mirror.set("a", "b");

        assertEquals(mirror, mirrorAfterSet);
    }

    @Test
    void createSetter() {
        final var lookup = MethodHandles.lookup();
        final var mirror = BeanMirror.of(Base.class, lookup).create();
        final var setter = mirror.createSetter("a", String.class);
        final var bean = mirror.get();
        setter.accept(bean, "b");

        assertEquals(mirror.get("a", String.class), "b");
    }

    @Test
    void createStaticSetter() {
        final var lookup = MethodHandles.lookup();
        final var mirror = BeanMirror.of(Target.class, lookup);
        final var staticSetter = mirror.createStaticSetter("value", Long.class);

        final var staticGetter = mirror.createStaticGetter("value", Long.class);
        assertEquals(staticGetter.get(), Long.valueOf(3L));
        assertEquals(mirror.get("value", Long.class), (Long)3L);
        staticSetter.accept(4L);
        assertEquals(mirror.get("value", Long.class), (Long)4L);
    }
}
