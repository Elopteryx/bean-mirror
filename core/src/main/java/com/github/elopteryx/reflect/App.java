package com.github.elopteryx.reflect;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static void main(String... args) throws Exception {

        final Target target = new Target();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final BeanMirror<Target> beanMirror = BeanMirror.of(target, lookup);
        final Function<Target, Character> getter = beanMirror.createGetter("b", char.class);
        final Supplier<Long> staticGetter = beanMirror.createStaticGetter("value", Long.class);

        System.out.println(getter.apply(target));
        System.out.println(staticGetter.get());


        final Child child = new Child();

        final BeanMirror<Child> castedMirror = BeanMirror.of(child, lookup);
        System.out.println(castedMirror.asType(Base.class).createGetter("a", char.class).apply(child));
        System.out.println(castedMirror.asType(Child.class).createGetter("c", char.class).apply(child));

        final Child newChild = BeanMirror.of(Child.class, lookup).create().get();

    }

}
