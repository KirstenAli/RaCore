package org.racore;

import org.racore.Request;

import java.util.function.Function;
import java.util.regex.Pattern;

public class EndpointHandler {
    private final String method;
    private final Pattern pattern;
    private final Function<Request, ?> callback;

    public EndpointHandler(String method, Pattern pattern, Function<Request, ?> callback) {
        this.method = method;
        this.pattern = pattern;
        this.callback = callback;
    }

    public String getMethod() {
        return method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Function<Request, ?> getCallback() {
        return callback;
    }
}
