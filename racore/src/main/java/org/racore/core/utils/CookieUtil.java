package org.racore.core.utils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.time.Duration;
import java.util.*;

public final class CookieUtil {
    private CookieUtil() {}

    public static Optional<String> readCookie(HttpExchange ex, String name) {
        Headers headers = ex.getRequestHeaders();
        List<String> cookieHeaders = headers.get("Cookie");
        if (cookieHeaders == null) return Optional.empty();

        for (String header : cookieHeaders) {
            String[] parts = header.split(";");
            for (String part : parts) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(name)) {
                    return Optional.of(kv[1]);
                }
            }
        }
        return Optional.empty();
    }

    public static void setCookie(HttpExchange ex, String name, String value, CookieOptions opts) {
        ex.getResponseHeaders().add("Set-Cookie", buildSetCookie(name, value, opts));
    }

    public static void clearCookie(HttpExchange ex, String name, CookieOptions opts) {
        CookieOptions cleared = opts.withMaxAge(Duration.ZERO);
        ex.getResponseHeaders().add("Set-Cookie", buildSetCookie(name, "", cleared));
    }

    public static String buildSetCookie(String name, String value, CookieOptions opts) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value == null ? "" : value);

        sb.append("; Path=").append(opts.path());

        if (opts.httpOnly()) sb.append("; HttpOnly");
        if (opts.secure()) sb.append("; Secure");

        if (opts.sameSite() != null) sb.append("; SameSite=").append(opts.sameSite());

        if (opts.maxAge() != null) {
            long seconds = Math.max(0, opts.maxAge().getSeconds());
            sb.append("; Max-Age=").append(seconds);
        }

        return sb.toString();
    }

    public record CookieOptions(
            String path,
            boolean httpOnly,
            boolean secure,
            String sameSite,
            Duration maxAge
    ) {
        public CookieOptions {
            if (path == null || path.isBlank()) path = "/";
        }

        public CookieOptions withMaxAge(Duration d) {
            return new CookieOptions(path, httpOnly, secure, sameSite, d);
        }
    }
}