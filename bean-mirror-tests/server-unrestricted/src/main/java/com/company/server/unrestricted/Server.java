package com.company.server.unrestricted;

public class Server {

    private String name;

    private String internalName = "$$INTERNAL$$";

    private long value = 4;

    public Server(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void internalRunnable(String str) {
        System.out.println(str + " " + str);
    }

    private String internalCallable(String str) {
        return str + " " + str;
    }
}
