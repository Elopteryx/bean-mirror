package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

public class BeanMirrorTest {

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

        System.out.println(getter.apply(target));
        System.out.println(staticGetter.get());


        final var child = new Child();

        final var castedMirror = BeanMirror.of(child, lookup);
        System.out.println(castedMirror.createGetter("a", String.class).apply(child));
        System.out.println(castedMirror.asType(Base.class).createGetter("a", String.class).apply(child));
        System.out.println(castedMirror.asType(Child.class).createGetter("c", char.class).apply(child));

        final var newChild = BeanMirror.of(Child.class, lookup).create().get();

    }
}
