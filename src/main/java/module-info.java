module org.racore {
    requires jdk.httpserver;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.commons.fileupload2.jakarta.servlet6;
    requires org.apache.commons.fileupload2.core;
    exports org.racore.interceptors;
    exports org.racore.config;
    exports org.racore.handlers;
    exports org.racore.servers;
    exports org.racore.core;
    exports org.racore.core.utils;
    exports org.racore.core.requests;
    exports org.racore.core.session;
    exports org.racore.core.responses;
}