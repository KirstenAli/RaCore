package org.racore.core.responses;

import java.io.OutputStream;

@FunctionalInterface
public interface StreamingResponse {
    void write(OutputStream outputStream) throws Exception;
}