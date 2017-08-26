
module com.company.server {
    exports com.company.server to com.company.client, com.company.client.unrestricted;
    opens com.company.server to com.company.client.unrestricted;
}