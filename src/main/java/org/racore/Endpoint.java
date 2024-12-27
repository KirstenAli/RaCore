package org.racore;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.racore.util.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
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
    private static final String STATIC_DIRECTORY = "src/main/resources/static";

    static {
        initializeServer();
    }

    public static void initializeServer() {
        server = ServerRegistry.getServer();
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
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

    public static void serveStatic() {
        registerEndpoint("GET","/*", Endpoint::handleStaticRequest);
    }

    private static Path handleStaticRequest(Request request) {
        return resolvePath(request.getPath());
    }

    public static Path resolvePath(String requestPath) {
        if (requestPath == null || requestPath.isEmpty()) {
            return null;
        }

        Path fullPath = Paths.get(STATIC_DIRECTORY, requestPath).normalize();
        return isValidFilePath(fullPath) ? fullPath : null;
    }

    private static boolean isValidFilePath(Path path) {
        return Files.exists(path) && !Files.isDirectory(path) && path.startsWith(STATIC_DIRECTORY);
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
        Map<String, String> queryParams = QueryParameterUtil.parseQueryParameters(exchange.getRequestURI());

        CustomRequest customRequest = FormDataExtractor.extractFormData(exchange);
        Map<String, DiskFileItem> uploadedFiles = customRequest.files();
        Map<String, String> formFields = customRequest.formFields();

        Request request = new Request(exchange, pathVariables, queryParams, uploadedFiles, formFields);

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

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = JsonUtils.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, jsonResponse);
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private record MatchedEndpoint(EndpointHandler handler, Matcher matcher) {
    }
}
