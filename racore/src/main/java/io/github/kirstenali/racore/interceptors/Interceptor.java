package io.github.kirstenali.racore.interceptors;

import com.sun.net.httpserver.HttpExchange;

public interface Interceptor {
    boolean preHandle(HttpExchange exchange);
    Object postHandle(Object response, HttpExchange exchange);
}
