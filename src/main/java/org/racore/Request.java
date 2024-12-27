package org.racore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import org.apache.commons.fileupload2.core.DiskFileItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
public class Request {
    private final HttpExchange exchange;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> queryParams;
    private final Map<String, String> pathVariables;
    private final Map<String, DiskFileItem> uploadedFiles;
    private final Map<String, String> formFields;

    public Request(HttpExchange exchange, Map<String, String> pathVariables, Map<String, String> queryParams,
                   Map<String, DiskFileItem> uploadedFiles, Map<String, String> formFields) {
        this.exchange = exchange;
        this.pathVariables = pathVariables;
        this.queryParams = queryParams;
        this.uploadedFiles = uploadedFiles;
        this.formFields = formFields;
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    public String getParam(String key) {
        return queryParams.get(key);
    }

    public String getPathVariable(String key) {
        return pathVariables.get(key);
    }

    public <T> T getBodyAs(Class<T> type) {
        try {
            if (isFormData()) {
                return parseFormDataAsObject(type);
            } else if (isJsonContentType()) {
                return parseJsonBodyAsObject(type);
            }
            throw new IllegalStateException("Unsupported content type: " + getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse request body as " + type.getSimpleName(), e);
        }
    }

    private boolean isFormData() {
        return !formFields.isEmpty();
    }

    private boolean isJsonContentType() {
        String contentType = getContentType();
        return "application/json".equalsIgnoreCase(contentType);
    }

    private String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    private <T> T parseFormDataAsObject(Class<T> type) {
        return objectMapper.convertValue(formFields, type);
    }

    private <T> T parseJsonBodyAsObject(Class<T> type) throws IOException {
        String body = readRequestBody();
        return objectMapper.readValue(body, type);
    }

    private String readRequestBody() throws IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line).append("\n");
            }
        }
        return requestBody.toString().trim();
    }

    public DiskFileItem getUploadedFile(String fileName) {
        return uploadedFiles.get(fileName);
    }

    public String getFormField(String fieldName) {
        return formFields.get(fieldName);
    }
}
