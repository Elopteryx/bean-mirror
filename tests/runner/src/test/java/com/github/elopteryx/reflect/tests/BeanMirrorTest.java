package com.github.elopteryx.reflect.tests;


import com.company.client.Client;
import org.junit.Test;

public class BeanMirrorTest {

    @Test
    public void legalAccess() throws Exception {
        com.company.client.unrestricted.Client.main();
    }

    @Test(expected = IllegalAccessException.class)
    public void unauthorizedAccess() throws Exception {
        Client.main();
    }
}
