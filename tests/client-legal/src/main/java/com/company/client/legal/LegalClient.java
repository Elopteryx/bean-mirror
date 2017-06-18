package com.company.client.legal;

import com.company.server.Server;
import com.github.elopteryx.reflect.BeanMirror;

import java.lang.invoke.MethodHandles;

public class LegalClient {

    public static void main(String... args) throws Exception {

        final Server server = new Server("server");

        BeanMirror<Server> mirror = BeanMirror.of(server).withLookup(MethodHandles.lookup());

        System.out.println(mirror.field("internalName", String.class).get());
        mirror.set("internalName", "Not so internal.");
        System.out.println(mirror.field("internalName", String.class).get());

        System.out.println(mirror.call("internalMethod", "abc").get());
    }
}