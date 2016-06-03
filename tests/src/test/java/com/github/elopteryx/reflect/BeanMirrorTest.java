package com.github.elopteryx.reflect;

import org.junit.Test;

public class BeanMirrorTest {

    @Test
    public void of() throws Exception {

    }

    @Test
    public void of1() throws Exception {

    }

    @Test
    public void set() throws Exception {

    }

    @Test
    public void get() throws Exception {

    }

    @Test
    public void get1() throws Exception {

    }

    @Test
    public void fieldWithClassType() throws Exception {
        String s = BeanMirror.of(new PlainObject())
                .field("s", String.class)
                .value()
                .orElse("");
    }

    @Test
    public void fieldWithoutClassType() throws Exception {
        String s = BeanMirror.of(new PlainObject())
                .field("s")
                .value()
                .map(String.class::cast)
                .orElse("");
    }

    public class PlainObject {

        private String s;

    }
}