package com.github.elopteryx.reflect.tests.astype;

@SuppressWarnings("unused")
public class Child implements Parent {

    private char c = 'c';

    @Override
    public int call() {
        return 1;
    }
}
