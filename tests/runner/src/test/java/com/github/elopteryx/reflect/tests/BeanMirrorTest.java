package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanMirrorTest {

    @Test
    void legalAccess() throws Throwable {
        com.company.client.unrestricted.Client.main();
    }

    @Test
    void unauthorizedAccess() throws Exception {
        final RuntimeException exception = assertThrows(RuntimeException.class, Client::main);
    }
}
