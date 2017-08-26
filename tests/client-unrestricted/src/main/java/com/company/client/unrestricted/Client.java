package com.company.client.unrestricted;

import com.company.server.Server;
import com.github.elopteryx.reflect.BeanMirror;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class Client {

    public static void main(String... args) throws Throwable {

        final Server server = new Server("server");

        BeanMirror<Server> mirror = BeanMirror.of(server, MethodHandles.lookup());

        System.out.println(mirror.field("internalName", String.class).get());
        mirror.set("internalName", "Not so internal.");
        System.out.println(mirror.field("internalName", String.class).get());

        System.out.println(mirror.call(String.class, "internalMethod", "abc").get());

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(server.getClass(), lookup);
        final Field f = Server.class.getDeclaredField("value");
        MethodHandle get = lookup.findVirtual(Field.class, "getLong",
                MethodType.methodType(long.class, Object.class));
    }
}