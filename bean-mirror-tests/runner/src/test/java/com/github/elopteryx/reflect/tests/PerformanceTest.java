package com.github.elopteryx.reflect.tests;

import com.github.elopteryx.reflect.BeanMirror;
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
import java.util.function.Function;

@SuppressWarnings("unused")
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class PerformanceTest {

    private int value = 42;

    private int value2 = 42;

    private int value3 = 42;

    private int value4 = 42;

    //private static final PerformanceTest INSTANCE = new PerformanceTest();

    private static final Field static_reflective;
    private static final MethodHandle static_unReflect;
    private static final MethodHandle static_mh;
    private static final Function<PerformanceTest, Integer> static_getter;

    private static Field reflective;
    private static MethodHandle unReflect;
    private static MethodHandle mh;
    private static Function<PerformanceTest, Integer> getter;

    private static Map<String, Object> accessors = new HashMap<>();

    static {
        try {
            reflective = PerformanceTest.class.getDeclaredField("value");
            unReflect = MethodHandles.lookup().unreflectGetter(reflective);
            mh = MethodHandles.lookup().findGetter(PerformanceTest.class, "value", int.class);

            var reflective2 = PerformanceTest.class.getDeclaredField("value2");
            accessors.put("value2", reflective2);

            var mh3 = MethodHandles.lookup().unreflectGetter(PerformanceTest.class.getDeclaredField("value3"));
            accessors.put("value3", mh3);

            var mh4 = MethodHandles.lookup().findGetter(PerformanceTest.class, "value4", int.class);
            accessors.put("value4", mh4);

            getter = BeanMirror.of(new PerformanceTest(), MethodHandles.lookup()).createGetter("value", Integer.class);
            static_getter = getter;

            static_reflective = reflective;
            static_unReflect = unReflect;
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
        return (int) PerformanceTest.class.getDeclaredField("value2").get(this);
    }

    @Benchmark
    public int dynamic_mh_without_caching() throws Throwable {
        return (int) MethodHandles.lookup().findGetter(PerformanceTest.class, "value2", int.class).invokeExact(this);
    }

    // WITHOUT MAP

    @Benchmark
    public int dynamic_reflect() throws IllegalAccessException {
        return (int) reflective.get(this);
    }

    @Benchmark
    public int dynamic_unreflect_invoke() throws Throwable {
        return (int) unReflect.invoke(this);
    }

    @Benchmark
    public int dynamic_unreflect_invokeExact() throws Throwable {
        return (int) unReflect.invokeExact(this);
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
        return (int) static_unReflect.invoke(this);
    }

    @Benchmark
    public int static_unreflect_invokeExact() throws Throwable {
        return (int) static_unReflect.invokeExact(this);
    }

    @Benchmark
    public int static_mh_invoke() throws Throwable {
        return (int) static_mh.invoke(this);
    }

    @Benchmark
    public int static_mh_invokeExact() throws Throwable {
        return (int) static_mh.invokeExact(this);
    }

    @Benchmark
    public int getter_apply() {
        return getter.apply(this);
    }

    @Benchmark
    public int static_getter_apply() {
        return static_getter.apply(this);
    }

}
