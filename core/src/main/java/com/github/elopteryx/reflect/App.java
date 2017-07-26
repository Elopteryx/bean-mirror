package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.function.LongGetter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
import java.util.Queue;

public class App {

    public static class Target {
        private long value = 3;
    }

    static final MethodHandles.Lookup trusted;

    static {
        try {
            final MethodHandles.Lookup original = MethodHandles.lookup();
            final Field internal = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            internal.setAccessible(true);
            trusted = (MethodHandles.Lookup) internal.get(original);
        } catch (final Throwable e) {
            throw new RuntimeException("Missing trusted lookup", e);
        }
    }

    public static void main(String... args) throws Exception {

        final Queue<Integer> queue = new PriorityQueue<>();
        queue.add(3);
        queue.add(1);
        queue.add(2);

        System.out.println(queue.poll());
        System.out.println(queue.peek());
        System.out.println(queue.peek());

        final Target target = new Target();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final LongGetter<Target> getter = BeanMirror.of(target, trusted).getterForLong("value", lookup);

        System.out.println(getter.apply(target));

    }

}
