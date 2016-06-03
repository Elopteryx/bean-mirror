package com.github.elopteryx.reflect.internal;

import java.util.Objects;
import java.util.function.Function;

abstract class Try<T> {

    private T result;

    private Try(T result) {
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    <U> Try<U> map(CheckedFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (result == null) {
            return (Try<U>) this;
        } else {
            return Try.of(result, mapper);
        }
    }

    <R> R orElse(Function<Exception, ? extends R> exceptionFunction) {
        if (result != null) {
            return (R)result;
        } else {
            return exceptionFunction.apply(((Failure)this).exception);
        }
    }

    T orElseThrow() throws Exception {
        if (result != null) {
            return result;
        } else {
            throw ((Failure)this).exception;
        }
    }

    <X extends Throwable> T orElseThrow(Function<Exception, ? extends X> exceptionFunction) throws X {
        if (result != null) {
            return result;
        } else {
            throw exceptionFunction.apply(((Failure)this).exception);
        }
    }

    static <R> Try<R> of(CheckedSupplier<R> supplier) {
        try {
            R result = supplier.get();
            return new Success<>(result);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    private static <T, R> Try<R> of(T input, CheckedFunction<? super T, ? extends R> function) {
        try {
            R result = function.apply(input);
            return new Success<>(result);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }


    private static class Success<S> extends Try<S> {

        private Success(S result) {
            super(result);
        }
    }

    private static class Failure<S> extends Try<S> {

        private Exception exception;

        private Failure(Exception exception) {
            super(null);
            this.exception = exception;
        }
    }

}
