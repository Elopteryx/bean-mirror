package com.github.elopteryx.reflect.tests;


import com.company.client.legal.LegalClient;
import com.company.client.unauthorized.UnAuthorizedClient;
import org.junit.Test;

public class BeanMirrorTest {

    @Test
    public void legalAccess() throws Exception {
        LegalClient.main();
    }

    @Test(expected = IllegalAccessException.class)
    public void unauthorizedAccess() throws Exception {
        UnAuthorizedClient.main();
    }
}
