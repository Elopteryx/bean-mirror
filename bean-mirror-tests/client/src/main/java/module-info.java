
module com.company.client {
    requires com.company.server;
    requires com.company.server.unrestricted;
    requires com.github.elopteryx.reflect;
    exports com.company.client;
}