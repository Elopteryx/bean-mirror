package com.company.client;

import com.github.elopteryx.reflect.BeanMirror;

import java.lang.invoke.MethodHandles;

public class Client {

    public static void unauthorizedAccess() throws Exception {

        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        BeanMirror<?> mirror = BeanMirror.of(Class.forName("com.company.server.Server"), lookup).create("server");

        System.out.println(mirror.field("internalName", String.class).get());
        mirror.set("internalName", "Not so internal.");
        System.out.println(mirror.field("internalName", String.class).get());

        System.out.println(mirror.call(String.class, "internalMethod", "abc").get());
    }

    public static void legalAccess() throws Exception {

        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        BeanMirror<?> mirror = BeanMirror.of(Class.forName("com.company.server.unrestricted.Server"), lookup).create("server");

        System.out.println(mirror.field("internalName", String.class).get());
        mirror.set("internalName", "Not so internal.");
        System.out.println(mirror.field("internalName", String.class).get());

        System.out.println(mirror.call(String.class, "internalMethod", "abc").get());
    }
}