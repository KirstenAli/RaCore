package org.racore.utils;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryParameterUtil {

    public static Map<String, String> parseQueryParameters(URI uri) {
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            return uriBuilder.getQueryParams().stream()
                    .collect(Collectors.toMap(
                            NameValuePair::getName,
                            param -> param.getValue() != null ? param.getValue() : ""
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse query parameters", e);
        }
    }
}
