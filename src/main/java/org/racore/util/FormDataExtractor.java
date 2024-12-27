package org.racore.util;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload2.core.*;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataExtractor {

    public static CustomRequest extractFormData(HttpExchange exchange) {
        Map<String, String> formFields = new HashMap<>();
        Map<String, DiskFileItem> files = new HashMap<>();

        DiskFileItemFactory factory = new DiskFileItemFactory.Builder().get();
        JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletFileUpload<>(factory);

        List<DiskFileItem> items;
        try {
            items = upload.parseRequest(new HttpExchangeRequestContext(exchange));
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }

        for (DiskFileItem item : items) {
            if (item.isFormField()) {
                formFields.put(item.getFieldName(), item.getString());
            } else {
                files.put(item.getFieldName(), item);
            }
        }

        return new CustomRequest(formFields, files);
    }
}
