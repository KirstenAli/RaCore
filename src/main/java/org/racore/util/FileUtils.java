package org.racore.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.*;

public class FileUtils {
    public static Path getFilePath(String directory, String fileName) {
        return Paths.get(directory, fileName).normalize();
    }

    public static byte[] readFile(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendStaticFileResponse(HttpExchange exchange, Path filePath) throws IOException {
        byte[] fileBytes = readFile(filePath);

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, fileBytes.length);

        try (var os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }
}
