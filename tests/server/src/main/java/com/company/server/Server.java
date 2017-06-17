package com.company.server;

public class Server {

    private String name;

    private String internalName = "$$INTERNAL$$";

    public Server(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String internalMethod(String str) {
        return str + " " + str;
    }
}
