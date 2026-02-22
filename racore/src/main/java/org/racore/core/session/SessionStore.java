package org.racore.core.session;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionStore {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Duration idleTimeout;

    public SessionStore(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Optional<Session> getIfPresentAndNotExpired(String id, long nowMs) {
        Session s = sessions.get(id);
        if (s == null) return Optional.empty();

        if (isExpired(s, nowMs)) {
            sessions.remove(id);
            return Optional.empty();
        }
        return Optional.of(s);
    }

    public Session putNew(Session session) {
        sessions.put(session.id(), session);
        return session;
    }

    public void remove(String id) {
        sessions.remove(id);
    }

    public int cleanupExpired(long nowMs) {
        int removed = 0;
        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Session> e = it.next();
            if (isExpired(e.getValue(), nowMs)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    private boolean isExpired(Session s, long nowMs) {
        long idleMs = nowMs - s.lastAccessMs();
        return idleMs > idleTimeout.toMillis();
    }

    public Duration idleTimeout() { return idleTimeout; }
}