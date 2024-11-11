package org.racore;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;

public class ServerRegistry {
    private static Function<Integer, HttpServer> serverFactory;

    public static void register(Function<Integer, HttpServer> factory) {
        serverFactory = factory;
    }

    public static HttpServer getServer() {
        int port = RaConfig.getServerPort();
        return (serverFactory != null) ? serverFactory.apply(port) : createDefaultHttpServer(port);
    }

    private static HttpServer createDefaultHttpServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(null);
            return server;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create HttpServer on port " + port, e);
        }
    }
}
