package org.racore.core.responses;

import com.sun.net.httpserver.HttpExchange;
import org.racore.core.utils.JsonUtils;

import java.io.IOException;

public final class ResponseWriters {
    private ResponseWriters() {}

    public static void stream(HttpExchange exchange, int statusCode, StreamingResponse body) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
        try (var os = exchange.getResponseBody()) {
            body.write(os);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void sse(HttpExchange exchange, int statusCode, StreamingResponse body) throws IOException {
        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/event-stream; charset=utf-8");
        headers.set("Cache-Control", "no-cache");

        stream(exchange, statusCode, body);
    }

    /**
     * Sends a JSON response using the provided {@code HttpExchange}.
     * <p>
     * This method sets the {@code Content-Type} header to {@code application/json},
     * serializes the {@code response} object to JSON, and then delegates to
     * {@link #sendResponse(HttpExchange, int, String)}.
     * </p>
     * <p>
     * <strong>Note:</strong> Using this method directly implies you are manually
     * handling the request and will bypass any post-interceptors. It is recommended
     * that you return {@code Handled.INSTANCE} from
     * your request handler to indicate you have fully handled the operation.
     * </p>
     *
     * @param exchange   the {@code HttpExchange} to send the response to
     * @param statusCode the HTTP status code to return
     * @param response   the response object to be serialized as JSON
     * @throws IOException if an I/O error occurs while writing the response
     */
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = JsonUtils.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, jsonResponse);
    }

    /**
     * Sends a plain text (or any custom format) response using the provided {@code HttpExchange}.
     * <p>
     * This method writes the given {@code response} string to the response body
     * and then closes the output stream.
     * </p>
     * <p>
     * <strong>Note:</strong> Using this method directly implies you are manually
     * handling the request and will bypass any post-interceptors. It is recommended
     * that you return {@code Handled.INSTANCE} from
     * your request handler to indicate you have fully handled the operation.
     * </p>
     *
     * @param exchange   the {@code HttpExchange} to send the response to
     * @param statusCode the HTTP status code to return
     * @param response   the response payload as a String
     * @throws IOException if an I/O error occurs while writing the response
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}