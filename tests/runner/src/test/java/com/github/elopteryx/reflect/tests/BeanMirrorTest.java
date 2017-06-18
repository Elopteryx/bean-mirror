package com.github.elopteryx.reflect.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.company.client.legal.LegalClient;
import com.company.client.unauthorized.UnAuthorizedClient;
import org.junit.jupiter.api.Test;

class BeanMirrorTest {

    @Test
    void legalAccess() throws Exception {
        LegalClient.main();
    }

    @Test
    void unauthorizedAccess() throws Exception {
        assertThrows(IllegalAccessException.class, UnAuthorizedClient::main);
    }
}
