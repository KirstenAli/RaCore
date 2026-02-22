package org.racore.core.session;

import com.sun.net.httpserver.HttpExchange;
import org.racore.core.utils.CookieUtil;

import java.security.SecureRandom;
import java.util.Base64;

public final class SessionManager {
    public static final String EXCHANGE_SESSION_ATTR = "racore.session";
    private final String cookieName;
    private final SessionStore store;
    private final CookieUtil.CookieOptions cookieOptions;

    private final SecureRandom rng = new SecureRandom();

    public SessionManager(String cookieName, SessionStore store, CookieUtil.CookieOptions cookieOptions) {
        this.cookieName = cookieName;
        this.store = store;
        this.cookieOptions = cookieOptions;
    }

    public Session current(HttpExchange ex) {
        Object v = ex.getAttribute(EXCHANGE_SESSION_ATTR);
        if (v instanceof Session s) return s;
        throw new IllegalStateException("Session not loaded. Did you register SessionInterceptor?");
    }

    public Session loadOrCreate(HttpExchange ex) {
        long now = System.currentTimeMillis();

        var sidOpt = CookieUtil.readCookie(ex, cookieName);

        if (sidOpt.isPresent()) {
            String sid = sidOpt.get();
            var existing = store.getIfPresentAndNotExpired(sid, now);
            if (existing.isPresent()) {
                Session s = existing.get();
                s.touch(now);
                ex.setAttribute(EXCHANGE_SESSION_ATTR, s);
                return s;
            }
        }

        String newId = newSessionId();
        Session session = new Session(newId, now);
        store.putNew(session);

        CookieUtil.CookieOptions opts = cookieOptions.withMaxAge(store.idleTimeout());
        CookieUtil.setCookie(ex, cookieName, newId, opts);

        ex.setAttribute(EXCHANGE_SESSION_ATTR, session);
        return session;
    }

    public void destroy(HttpExchange ex) {
        Session s = (Session) ex.getAttribute(EXCHANGE_SESSION_ATTR);
        if (s != null) store.remove(s.id());
        CookieUtil.clearCookie(ex, cookieName, cookieOptions);
        ex.setAttribute(EXCHANGE_SESSION_ATTR, null);
    }

    private String newSessionId() {
        byte[] bytes = new byte[24];
        rng.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}