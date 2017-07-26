package com.github.elopteryx.reflect;

import com.github.elopteryx.reflect.function.LongGetter;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

// Java 8 generic LambdaMetafactory?
// http://stackoverflow.com/questions/28196829/java-8-generic-lambdametafactory

// Black magic solution for: "Java 8 access private member with lambda?"
// http://stackoverflow.com/questions/28184065/java-8-access-private-member-with-lambda

class Target3 {

    private int id;

    Target3(final int id) {
        this.id = id;
    }

    private int id() {
        return id;
    }

    private void id(final int id) {
        this.id = id;
    }
}

public class PrivateTargetLambdaWorking {

    static Method functionMethod(final Class<?> klaz, final String name) {
        Method result = null;
        final Method[] methodList = klaz.getDeclaredMethods();
        for (final Method method : methodList) {
            if (method.getName().equals(name)) {
                if (result == null) {
                    result = method;
                } else {
                    throw new RuntimeException("Duplicate method: " + name);
                }
            }
        }
        if (result == null) {
            throw new RuntimeException("Missing method: " + name);
        } else {
            return result;
        }
    }

    static MethodType functionType(final Class<?> klaz, final String name) {
        final Method method = functionMethod(klaz, name);
        final Class<?> methodReturn = method.getReturnType();
        final Class<?>[] methodParams = method.getParameterTypes();
        return MethodType.methodType(methodReturn, methodParams);
    }

    static <T> T produceLambda( //
                                final Lookup caller, //
                                final Class<T> functionKlaz, //
                                final String functionName, //
                                final MethodHandle implementationMethod //
    ) throws Throwable {

        final MethodType factoryMethodType = //
                MethodType.methodType(functionKlaz);

        final MethodType functionMethodType = //
                functionType(functionKlaz, functionName);

        final CallSite lambdaFactory = LambdaMetafactory.metafactory( //
                caller, // Represents a lookup context.
                functionName, // The name of the method to implement.
                factoryMethodType, // Signature of the factory method.
                functionMethodType, // Signature of function implementation.
                implementationMethod, // Function method implementation.
                implementationMethod.type() // Function method type signature.
        );

        final MethodHandle factoryInvoker = lambdaFactory.getTarget();

        final T lambda = (T) factoryInvoker.invoke();

        return lambda;
    }

    static ToIntFunction getterLambda(final Lookup caller,
                                      final MethodHandle getterHandle) throws Throwable {
        return produceLambda(caller, ToIntFunction.class, "applyAsInt",
                getterHandle);
    }

    static <T, R> Function<T, R> getterLambda2(final Lookup caller,
                                               final MethodHandle getterHandle) throws Throwable {
        return produceLambda(caller, Function.class, "apply",
                getterHandle);
    }

    public static <T> LongGetter<T> getterLambdaLong(final Lookup caller,
                                               final MethodHandle getterHandle) throws Throwable {
        return produceLambda(caller, LongGetter.class, "apply",
                getterHandle);
    }

    static ObjIntConsumer setterLambda(final Lookup caller,
                                       final MethodHandle setterHandle) throws Throwable {
        return produceLambda(caller, ObjIntConsumer.class, "accept",
                setterHandle);
    }

    static  Lookup trusted;

    static {
        try {
            final Lookup original = MethodHandles.lookup();
            final Field internal = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            internal.setAccessible(true);
            trusted = (Lookup) internal.get(original);
        } catch (final Throwable e) {
            //throw new RuntimeException("Missing trusted lookup", e);
        }
    }

    public static Lookup lookup(final Class<?> klaz) {
        return trusted.in(klaz);
    }

    public static void main(final String... args) throws Throwable {

        final Lookup caller = lookup(Target3.class);

        final Method getterMethod = Target3.class.getDeclaredMethod("id");
        final Method setterMethod = Target3.class.getDeclaredMethod("id",
                int.class);

        final MethodHandle getterHandle = caller.unreflect(getterMethod);
        final MethodHandle setterHandle = caller.unreflect(setterMethod);

        final ToIntFunction getterLambda = getterLambda(caller, getterHandle);
        final ObjIntConsumer setterLambda = setterLambda(caller, setterHandle);

        final int set1 = 123;

        final Target3 target = new Target3(set1);

        final int get1 = getterLambda.applyAsInt(target);

        if (get1 != set1) {
            throw new Error("Getter failure.");
        } else {
            System.out.println("Getter success.");
        }

        final int set2 = 456;

        setterLambda.accept(target, set2);

        final int get2 = getterLambda.applyAsInt(target);

        if (get2 != set2) {
            throw new Error("Setter failure.");
        } else {
            System.out.println("Setter success.");
        }

    }

}
