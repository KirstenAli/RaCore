package org.racore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Request {
    private final HttpExchange exchange;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> pathVariables;
    private final String body;
    private final Map<String, File> uploadedFiles = new HashMap<>();
    private final Map<String, String> formFields = new HashMap<>();

    public Request(HttpExchange exchange, Map<String, String> pathVariables) throws IOException {
        this.exchange = exchange;
        this.pathVariables = pathVariables;
        this.body = readRequestBody(exchange);
        parseQueryParameters(exchange.getRequestURI());

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null) {
            if (contentType.startsWith("multipart/form-data")) {
                processMultipartFormData(exchange);
            } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
                processFormUrlEncodedData(this.body);
            }
        }
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line).append("\n");
            }
            return requestBody.toString().trim();
        }
    }

    private void parseQueryParameters(URI uri) {
        if (uri.getQuery() != null) {
            for (String param : uri.getQuery().split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    queryParams.put(pair[0], decodeUrlComponent(pair[1]));
                }
            }
        }
    }

    private void processFormUrlEncodedData(String body) {
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = decodeUrlComponent(keyValue[0]);
                String value = decodeUrlComponent(keyValue[1]);
                formFields.put(key, value);
            }
        }
    }

    private String decodeUrlComponent(String component) {
        return java.net.URLDecoder.decode(component, StandardCharsets.UTF_8);
    }

    private void processMultipartFormData(HttpExchange exchange) throws IOException {
        String boundary = "--" + extractBoundary(exchange.getRequestHeaders().getFirst("Content-Type"));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(boundary)) {
                    processPart(reader, boundary);
                }
            }
        }
    }

    private String extractBoundary(String contentType) {
        return contentType.split("boundary=")[1].trim();
    }

    private void processPart(BufferedReader reader, String boundary) throws IOException {
        String contentDisposition = reader.readLine();
        String name = extractName(contentDisposition);
        String fileName = extractFileName(contentDisposition);

        if (fileName != null) {
            processFilePart(reader, name, fileName, boundary);
        } else if (name != null) {
            processFormFieldPart(reader, name, boundary);
        }
    }

    private String extractFileName(String contentDisposition) {
        if (contentDisposition == null || !contentDisposition.contains("filename=\"")) {
            return null;
        }
        return contentDisposition.split("filename=\"")[1].split("\"")[0];
    }

    private String extractName(String contentDisposition) {
        if (contentDisposition == null || !contentDisposition.contains("name=\"")) {
            return null;
        }
        return contentDisposition.split("name=\"")[1].split("\"")[0];
    }

    private void processFilePart(BufferedReader reader, String name, String fileName, String boundary) throws IOException {
        skipHeaders(reader);

        Path tempFile = Files.createTempFile("upload-", "-" + fileName);
        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            String line;
            while ((line = reader.readLine()) != null && !line.startsWith(boundary)) {
                outputStream.write((line + "\n").getBytes());
            }
        }
        uploadedFiles.put(name, tempFile.toFile());
    }

    private void processFormFieldPart(BufferedReader reader, String name, String boundary) throws IOException {
        skipHeaders(reader);

        StringBuilder fieldContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.startsWith(boundary)) {
            fieldContent.append(line).append("\n");
        }
        formFields.put(name, fieldContent.toString().trim());
    }

    private void skipHeaders(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // Skipping headers
        }
    }

    public String getParam(String key) {
        return queryParams.get(key);
    }

    public String getPathVariable(String key) {
        return pathVariables.get(key);
    }

    public String getBody() {
        return body;
    }

    public <T> T getBodyAs(Class<T> type) {
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public File getUploadedFile(String fileName) {
        return uploadedFiles.get(fileName);
    }

    public String getFormField(String fieldName) {
        return formFields.get(fieldName);
    }
}
