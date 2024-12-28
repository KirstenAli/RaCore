package org.racore.utils;

import org.apache.commons.fileupload2.core.RequestContext;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;

public class HttpExchangeRequestContext implements RequestContext {
    private final HttpExchange exchange;

    public HttpExchangeRequestContext(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    @Override
    public long getContentLength() {
        String length = exchange.getRequestHeaders().getFirst("Content-Length");
        return length != null ? Integer.parseInt(length) : -1;
    }

    @Override
    public InputStream getInputStream() {
        return exchange.getRequestBody();
    }
}
