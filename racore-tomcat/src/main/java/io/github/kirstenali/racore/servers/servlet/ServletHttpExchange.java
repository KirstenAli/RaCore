package io.github.kirstenali.racore.servers.servlet;

import com.sun.net.httpserver.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

public final class ServletHttpExchange extends HttpExchange {

    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final Map<String, Object> attributes;

    private final URI requestUri;
    private final DummyHttpContext context;

    private int responseCode;

    public ServletHttpExchange(HttpServletRequest req, HttpServletResponse resp) {
        this.req = Objects.requireNonNull(req, "HttpServletRequest must not be null");
        this.resp = Objects.requireNonNull(resp, "HttpServletResponse must not be null");

        this.requestHeaders = copyRequestHeaders(this.req);
        this.responseHeaders = new Headers();
        this.attributes = new HashMap<>();

        this.requestUri = buildRequestUri(this.req);
        this.context = new DummyHttpContext(this.req.getRequestURI());

        this.responseCode = -1;
    }

    private static Headers copyRequestHeaders(HttpServletRequest req) {
        Headers headers = new Headers();

        Enumeration<String> names = req.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> values = req.getHeaders(name);
            while (values != null && values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }
        return headers;
    }

    private static URI buildRequestUri(HttpServletRequest req) {
        String qs = req.getQueryString();
        String full = req.getRequestURL().toString() + (qs != null ? "?" + qs : "");
        return URI.create(full);
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        return requestUri;
    }

    @Override
    public String getRequestMethod() {
        return req.getMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return context;
    }

    @Override
    public void close() {
        try {
            resp.flushBuffer();
        } catch (Exception ignored) {
        }
    }

    @Override
    public InputStream getRequestBody() {
        try {
            return req.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getResponseBody() {
        try {
            return resp.getOutputStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) {
        applyResponseHeaders(resp, responseHeaders);

        this.responseCode = rCode;
        resp.setStatus(rCode);

        if (responseLength > 0) {
            resp.setContentLengthLong(responseLength);
        } else if (responseLength < 0) {
            // HttpExchange: -1 means "no response body"
            resp.setContentLength(0);
        }
    }

    private static void applyResponseHeaders(HttpServletResponse resp, Headers responseHeaders) {
        for (Map.Entry<String, List<String>> e : responseHeaders.entrySet()) {
            String name = e.getKey();
            for (String v : e.getValue()) {
                resp.addHeader(name, v);
            }
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(req.getRemoteHost(), req.getRemotePort());
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(req.getLocalName(), req.getLocalPort());
    }

    @Override
    public String getProtocol() {
        return req.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        throw new UnsupportedOperationException("setStreams not supported in ServletHttpExchange");
    }

    @Override
    public HttpPrincipal getPrincipal() {
        if (req.getUserPrincipal() == null) return null;
        String username = req.getUserPrincipal().getName();
        return new HttpPrincipal(username, "");
    }

    private static final class DummyHttpContext extends HttpContext {
        private final String path;
        private final Map<String, Object> attrs;
        private HttpHandler handler;

        DummyHttpContext(String path) {
            this.path = path;
            this.attrs = new HashMap<>();
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public HttpServer getServer() {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attrs;
        }

        @Override
        public List<Filter> getFilters() {
            return List.of();
        }

        @Override
        public Authenticator setAuthenticator(Authenticator auth) {
            return null;
        }

        @Override
        public Authenticator getAuthenticator() {
            return null;
        }

        @Override
        public HttpHandler getHandler() {
            return handler;
        }

        @Override
        public void setHandler(HttpHandler handler) {
            this.handler = handler;
        }
    }
}