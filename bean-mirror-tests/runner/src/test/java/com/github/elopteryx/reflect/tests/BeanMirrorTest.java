package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import com.github.elopteryx.reflect.BeanMirror;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanMirrorTest {

    @Test
    void createNewObjectMirror() {
        final var object = new Object();
        final var lookup = MethodHandles.lookup();
        final var mirror = BeanMirror.of(object, lookup);
        assertEquals(object, mirror.get());
    }

    @Test
    void createObjectMirrorWithNullParams() {
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Object)null, MethodHandles.lookup()));
        assertThrows(NullPointerException.class, () -> BeanMirror.of(new Object(), null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Object)null, null));
    }

    @Test
    void createNewClassMirror() {
        final var lookup = MethodHandles.lookup();
        BeanMirror.of(Object.class, lookup);
    }

    @Test
    void createClassMirrorWithNullParams() {
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Class<?>)null, MethodHandles.lookup()));
        assertThrows(NullPointerException.class, () -> BeanMirror.of(Object.class, null));
        assertThrows(NullPointerException.class, () -> BeanMirror.of((Class<?>)null, null));
    }

    @Test
    void legalAccess() throws Exception {
        System.out.println("Running legalAccess");
        Client.legalAccess();
    }

    @Test
    void unauthorizedAccess() throws Exception {
        System.out.println("Running unauthorizedAccess");
        Client.unauthorizedAccess();
        // final var exception = assertThrows(RuntimeException.class, Client::unauthorizedAccess);
    }
}
