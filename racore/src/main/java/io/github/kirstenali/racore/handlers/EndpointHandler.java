package io.github.kirstenali.racore.handlers;

import io.github.kirstenali.racore.core.requests.Request;

import java.util.function.Function;
import java.util.regex.Pattern;

public record EndpointHandler(String method, Pattern pattern, Function<Request, ?> callback) {
}
