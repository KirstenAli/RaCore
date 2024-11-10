package org.racore;

import org.racore.util.FileUtils;
import org.racore.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Endpoint {
    private static final Map<String, EndpointHandler> endpoints = new HashMap<>();
    private static final List<Interceptor> interceptors = new ArrayList<>();
    private static HttpServer server;
    private static final String STATIC_DIRECTORY = "/static";

    static {
        int port = RaConfig.getServerPort();
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(null);
            server.start();
            System.out.println("Endpoint server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serveStatic();
    }

    public static void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static void get(String endpoint, Function<Request, ?> callback) {
        registerEndpoint("GET", endpoint, callback);
    }

    public static void post(String endpoint, Function<Request, ?> callback) {
        registerEndpoint("POST", endpoint, callback);
    }

    public static void put(String endpoint, Function<Request, ?> callback) {
        registerEndpoint("PUT", endpoint, callback);
    }

    public static void delete(String endpoint, Function<Request, ?> callback) {
        registerEndpoint("DELETE", endpoint, callback);
    }

    public static void patch(String endpoint, Function<Request, ?> callback) {
        registerEndpoint("PATCH", endpoint, callback);
    }

    private static void serveStatic() {
        registerEndpoint("GET","/*", Endpoint::handleStaticRequest);
    }

    public static void serveStatic(String path) {
        registerEndpoint("GET", path, Endpoint::handleStaticRequest);
    }

    private static Object handleStaticRequest(Request request) {
        String requestedPath = request.getPath();
        Path fullPath = Paths.get(STATIC_DIRECTORY + requestedPath).normalize();

        if (Files.exists(fullPath) && !Files.isDirectory(fullPath) && fullPath.startsWith(STATIC_DIRECTORY)) {
            return fullPath;
        } else {
            return null;
        }
    }

    private static void registerEndpoint(String method, String endpoint, Function<Request, ?> callback) {
        String regex = endpointToRegex(endpoint);
        EndpointHandler handler = new EndpointHandler(method, Pattern.compile(regex), callback);

        endpoints.put(endpoint, handler);
        server.createContext(endpoint.split("\\{")[0], Endpoint::handleExchange);
    }

    private static String endpointToRegex(String endpoint) {
        return endpoint.replaceAll("\\{[^}]+}", "([^/]+)");
    }

    private static void handleExchange(HttpExchange exchange) throws IOException {
        if (!runPreInterceptors(exchange)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        MatchedEndpoint matchedEndpoint = findMatchingEndpoint(path);

        if (matchedEndpoint == null) {
            sendResponse(exchange, 404, "Not Found");
            return;
        }

        Object response = processRequest(exchange, matchedEndpoint.handler, matchedEndpoint.matcher);
        response = runPostInterceptors(response, exchange);

        handleResponse(exchange, response);
    }


    private static boolean runPreInterceptors(HttpExchange exchange) {
        for (Interceptor interceptor : interceptors) {
            if (!interceptor.preHandle(exchange)) {
                return false;
            }
        }
        return true;
    }

    private static Object runPostInterceptors(Object response, HttpExchange exchange) {
        for (Interceptor interceptor : interceptors) {
            response = interceptor.postHandle(response, exchange);
        }
        return response;
    }

    private static void handleResponse(HttpExchange exchange, Object response) throws IOException {
        if (response == null) {
            sendResponse(exchange, 404, "Not Found");
        } else if (response instanceof Path) {
            FileUtils.sendStaticFileResponse(exchange, (Path) response);
        } else {
            sendJsonResponse(exchange, 200, response);
        }
    }

    private static Object processRequest(HttpExchange exchange, EndpointHandler handler, Matcher matcher) throws IOException {
        Map<String, String> pathVariables = extractPathVariables(matcher);
        Request request = new Request(exchange, pathVariables);
        return handler.getCallback().apply(request);
    }

    private static Map<String, String> extractPathVariables(java.util.regex.Matcher matcher) {
        Map<String, String> pathVariables = new HashMap<>();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            pathVariables.put("param" + (i - 1), matcher.group(i));
        }
        return pathVariables;
    }

    private static MatchedEndpoint findMatchingEndpoint(String path) {
        for (EndpointHandler handler : endpoints.values()) {
            Matcher matcher = handler.getPattern().matcher(path);
            if (matcher.matches()) {
                return new MatchedEndpoint(handler, matcher);
            }
        }
        return null;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = response instanceof String ? (String) response : JsonUtils.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static class MatchedEndpoint {
        final EndpointHandler handler;
        final Matcher matcher;

        MatchedEndpoint(EndpointHandler handler, Matcher matcher) {
            this.handler = handler;
            this.matcher = matcher;
        }
    }
}
