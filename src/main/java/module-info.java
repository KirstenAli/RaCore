module org.racore {
    requires jdk.httpserver;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.commons.fileupload2.jakarta.servlet6;
    requires org.apache.commons.fileupload2.core;
    exports org.racore;
}