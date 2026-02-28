package io.github.kirstenali.racore.core.session;

import com.sun.net.httpserver.HttpExchange;
import io.github.kirstenali.racore.core.utils.CookieUtil;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

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
        Object session = ex.getAttribute(EXCHANGE_SESSION_ATTR);
        if (session instanceof Session s) return s;
        throw new IllegalStateException("Session not loaded. Did you register SessionInterceptor?");
    }

    public Session loadOrCreate(HttpExchange ex) {
        long now = System.currentTimeMillis();

        return load(ex, now)
                .orElseGet(() -> create(ex, now));
    }

    private Optional<Session> load(HttpExchange ex, long now) {
        Optional<String> sidOpt = CookieUtil.readCookie(ex, cookieName);
        if (sidOpt.isEmpty()) return Optional.empty();

        String sid = sidOpt.get();
        Optional<Session> existing = store.getIfPresentAndNotExpired(sid, now);
        if (existing.isEmpty()) return Optional.empty();

        Session session = existing.get();
        session.touch(now);
        ex.setAttribute(EXCHANGE_SESSION_ATTR, session);
        return Optional.of(session);
    }

    public Session create(HttpExchange ex, long now) {
        String newId = newSessionId();
        Session session = new Session(newId, now);
        store.putNew(session);

        CookieUtil.CookieOptions opts = cookieOptions.withMaxAge(store.idleTimeout());
        CookieUtil.setCookie(ex, cookieName, newId, opts);

        ex.setAttribute(EXCHANGE_SESSION_ATTR, session);
        return session;
    }

    public void destroy(HttpExchange ex) {
        Session session = (Session) ex.getAttribute(EXCHANGE_SESSION_ATTR);
        if (session != null) store.remove(session.id());
        CookieUtil.clearCookie(ex, cookieName, cookieOptions);
        ex.setAttribute(EXCHANGE_SESSION_ATTR, null);
    }

    private String newSessionId() {
        byte[] bytes = new byte[24];
        rng.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}