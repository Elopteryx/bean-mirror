package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    @Test
    void tryThings() {

        final var target = new Target();
        final var lookup = MethodHandles.lookup();
        final var beanMirror = BeanMirror.of(target, lookup);
        final var getter = beanMirror.createGetter("b", char.class);
        final var staticGetter = beanMirror.createStaticGetter("value", Long.class);

        assertEquals((char)getter.apply(target), 'b');
        assertEquals(staticGetter.get(), Long.valueOf(3L));

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

        final var newChild = BeanMirror.of(Child.class, lookup).create().get();
        assertEquals(newChild.getClass(), Child.class);

    }
}
