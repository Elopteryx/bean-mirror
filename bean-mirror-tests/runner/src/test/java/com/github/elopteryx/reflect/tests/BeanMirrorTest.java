package com.github.elopteryx.reflect.tests;

import com.company.client.Client;
import org.junit.jupiter.api.Test;

public class BeanMirrorTest {

    @org.junit.Test
    @Test
    public void legalAccess() throws Throwable {
        Client.legalAccess();
    }

    @org.junit.Test
    @Test
    public void unauthorizedAccess() {
        // final var exception = assertThrows(RuntimeException.class, Client::unauthorizedAccess);
    }
}
