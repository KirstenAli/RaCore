package org.racore.core.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Session {
    private final String id;
    private final long createdAtMs;
    private volatile long lastAccessMs;
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public Session(String id, long nowMs) {
        this.id = id;
        this.createdAtMs = nowMs;
        this.lastAccessMs = nowMs;
    }

    public String id() { return id; }
    public long createdAtMs() { return createdAtMs; }
    public long lastAccessMs() { return lastAccessMs; }

    public void touch(long nowMs) { this.lastAccessMs = nowMs; }

    public Map<String, Object> data() { return data; }

    public Object get(String key) { return data.get(key); }
    public void put(String key, Object value) { data.put(key, value); }
    public void remove(String key) { data.remove(key); }
}