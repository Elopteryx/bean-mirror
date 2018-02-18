package com.github.elopteryx.reflect.tests;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class MHOpto {

    private int value = 42;

    private int value2 = 42;

    private int value3 = 42;

    private int value4 = 42;

    private static final Field static_reflective;
    private static final MethodHandle static_unreflect;
    private static final MethodHandle static_mh;

    private static Field reflective;
    private static MethodHandle unreflect;
    private static MethodHandle mh;

    private static Map<String, Object> accessors = new HashMap<>();

    // We would normally use @Setup, but we need to initialize "static final" fields here...
    static {
        try {
            reflective = MHOpto.class.getDeclaredField("value");
            unreflect = MethodHandles.lookup().unreflectGetter(reflective);
            mh = MethodHandles.lookup().findGetter(MHOpto.class, "value", int.class);

            var reflective2 = MHOpto.class.getDeclaredField("value2");
            accessors.put("value2", reflective2);

            var mh3 = MethodHandles.lookup().unreflectGetter(MHOpto.class.getDeclaredField("value3"));
            accessors.put("value3", mh3);

            var mh4 = MethodHandles.lookup().findGetter(MHOpto.class, "value4", int.class);
            accessors.put("value4", mh4);

            static_reflective = reflective;
            static_unreflect = unreflect;
            static_mh = mh;

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public int plain() {
        return value;
    }

    @Benchmark
    public int dynamic_reflect_without_caching() throws IllegalAccessException, NoSuchFieldException {
        return (int) MHOpto.class.getDeclaredField("value2").get(this);
    }

    @Benchmark
    public int dynamic_mh_without_caching() throws Throwable {
        return (int) MethodHandles.lookup().findGetter(MHOpto.class, "value2", int.class).invokeExact(this);
    }

    // WITHOUT MAP

    @Benchmark
    public int dynamic_reflect() throws IllegalAccessException {
        return (int) reflective.get(this);
    }

    @Benchmark
    public int dynamic_unreflect_invoke() throws Throwable {
        return (int) unreflect.invoke(this);
    }

    @Benchmark
    public int dynamic_unreflect_invokeExact() throws Throwable {
        return (int) unreflect.invokeExact(this);
    }

    @Benchmark
    public int dynamic_mh_invoke() throws Throwable {
        return (int) mh.invoke(this);
    }

    @Benchmark
    public int dynamic_mh_invokeExact() throws Throwable {
        return (int) mh.invokeExact(this);
    }

    // WITH MAP

    @Benchmark
    public int dynamic_reflect_with_map() throws IllegalAccessException {
        return (int) ((Field)accessors.get("value2")).get(this);
    }

    @Benchmark
    public int dynamic_unreflect_invoke_with_map() throws Throwable {
        return (int) ((MethodHandle)accessors.get("value3")).invoke(this);
    }

    @Benchmark
    public int dynamic_unreflect_invokeExact_with_map() throws Throwable {
        return (int) ((MethodHandle)accessors.get("value3")).invokeExact(this);
    }

    @Benchmark
    public int dynamic_mh_invoke_with_map() throws Throwable {
        return (int) ((MethodHandle)accessors.get("value4")).invoke(this);
    }

    @Benchmark
    public int dynamic_mh_invokeExact_with_map() throws Throwable {
        return (int) ((MethodHandle)accessors.get("value4")).invokeExact(this);
    }


    @Benchmark
    public int static_reflect() throws IllegalAccessException {
        return (int) static_reflective.get(this);
    }

    @Benchmark
    public int static_unreflect_invoke() throws Throwable {
        return (int) static_unreflect.invoke(this);
    }

    @Benchmark
    public int static_unreflect_invokeExact() throws Throwable {
        return (int) static_unreflect.invokeExact(this);
    }

    @Benchmark
    public int static_mh_invoke() throws Throwable {
        return (int) static_mh.invoke(this);
    }

    @Benchmark
    public int static_mh_invokeExact() throws Throwable {
        return (int) static_mh.invokeExact(this);
    }

}
