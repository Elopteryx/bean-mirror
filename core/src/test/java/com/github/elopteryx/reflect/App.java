package com.github.elopteryx.reflect;

import java.lang.invoke.MethodHandles;

public class App {

    public static class Base {
        protected char a = 'a';
    }

    public static class Target extends Base {
        private static Long value = 3L;
        protected char b = 'b';
    }

    public static class Child extends Target {
        protected char c = 'c';
    }

    public static void main(String... args) {

        final var target = new Target();
        final var lookup = MethodHandles.lookup();
        final var beanMirror = BeanMirror.of(target, lookup);
        final var getter = beanMirror.createGetter("b", char.class);
        final var staticGetter = beanMirror.createStaticGetter("value", Long.class);

        System.out.println(getter.apply(target));
        System.out.println(staticGetter.get());


        final var child = new Child();

        final var castedMirror = BeanMirror.of(child, lookup);
        System.out.println(castedMirror.asType(Base.class).createGetter("a", char.class).apply(child));
        System.out.println(castedMirror.asType(Child.class).createGetter("c", char.class).apply(child));

        final var newChild = BeanMirror.of(Child.class, lookup).create().get();

    }

}
