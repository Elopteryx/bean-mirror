package com.github.elopteryx.reflect.tests;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;

public class TestMethodPerf
{
    private static final int ITERATIONS = 50_000_000;
    private static final int WARM_UP = 10;

    public static void main(String... args) throws Throwable
    {
        // hold result to prevent too much optimizations
        final var dummy=new int[4];

        var reflected=TestMethodPerf.class
                .getDeclaredMethod("myMethod", int.class, int.class);
        final var lookup = MethodHandles.lookup();
        var mh=lookup.unreflect(reflected);
        var lambda=(IntBinaryOperator)LambdaMetafactory.metafactory(
                lookup, "applyAsInt", MethodType.methodType(IntBinaryOperator.class),
                mh.type(), mh, mh.type()).getTarget().invokeExact();

        for(var i = 0; i<WARM_UP; i++)
        {
            dummy[0]+=testDirect(dummy[0]);
            dummy[1]+=testLambda(dummy[1], lambda);
            dummy[2]+=testMH(dummy[1], mh);
            dummy[3]+=testReflection(dummy[2], reflected);
        }
        var t0=System.nanoTime();
        dummy[0]+=testDirect(dummy[0]);
        var t1=System.nanoTime();
        dummy[1]+=testLambda(dummy[1], lambda);
        var t2=System.nanoTime();
        dummy[2]+=testMH(dummy[1], mh);
        var t3=System.nanoTime();
        dummy[3]+=testReflection(dummy[2], reflected);
        var t4=System.nanoTime();
        System.out.printf("direct: %.2fs, lambda: %.2fs, mh: %.2fs, reflection: %.2fs%n",
                (t1-t0)*1e-9, (t2-t1)*1e-9, (t3-t2)*1e-9, (t4-t3)*1e-9);

        // do something with the results
        if(dummy[0]!=dummy[1] || dummy[0]!=dummy[2] || dummy[0]!=dummy[3])
            throw new AssertionError();
    }

    private static int testMH(int v, MethodHandle mh) throws Throwable
    {
        for(var i = 0; i<ITERATIONS; i++)
            v+=(int)mh.invokeExact(1000, v);
        return v;
    }

    private static int testReflection(int v, Method mh) throws Throwable
    {
        for(var i = 0; i<ITERATIONS; i++)
            v+=(int)mh.invoke(null, 1000, v);
        return v;
    }

    private static int testDirect(int v)
    {
        for(var i = 0; i<ITERATIONS; i++)
            v+=myMethod(1000, v);
        return v;
    }

    private static int testLambda(int v, IntBinaryOperator accessor)
    {
        for(var i = 0; i<ITERATIONS; i++)
            v+=accessor.applyAsInt(1000, v);
        return v;
    }

    private static int myMethod(int a, int b)
    {
        return a<b? a: b;
    }
}
