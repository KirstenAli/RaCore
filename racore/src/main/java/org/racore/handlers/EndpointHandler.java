package org.racore.handlers;

import org.racore.core.requests.Request;

import java.util.function.Function;
import java.util.regex.Pattern;

public record EndpointHandler(String method, Pattern pattern, Function<Request, ?> callback) {
}
