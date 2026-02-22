package org.racore.interceptors;

import com.sun.net.httpserver.HttpExchange;
import org.racore.core.session.SessionManager;

public final class SessionInterceptor implements Interceptor {
    private final SessionManager sessions;

    public SessionInterceptor(SessionManager sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpExchange exchange) {
        sessions.loadOrCreate(exchange);
        return true;
    }

    @Override
    public Object postHandle(Object response, HttpExchange exchange) {
        return response;
    }
}