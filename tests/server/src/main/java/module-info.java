
module com.company.server {
    exports com.company.server to com.company.client.legal, com.company.client.unauthorized;
    opens com.company.server to com.company.client.legal;
}