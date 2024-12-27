package org.racore;

import java.util.function.Function;
import java.util.regex.Pattern;

public record EndpointHandler(String method, Pattern pattern, Function<Request, ?> callback) {
}
