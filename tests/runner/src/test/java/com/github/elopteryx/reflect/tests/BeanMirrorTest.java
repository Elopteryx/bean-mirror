package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BeanMirrorTest {

    @org.junit.Test
    @Test
    public void legalAccess() throws Throwable {
        Client.legalAccess();
    }

    @org.junit.Test
    @Test
    public void unauthorizedAccess() throws Exception {
        final RuntimeException exception = assertThrows(RuntimeException.class, Client::unauthorizedAccess);
    }
}
