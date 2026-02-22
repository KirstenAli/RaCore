package org.racore.servers.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.racore.core.Endpoint;

import java.io.IOException;

public final class RaCoreServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Endpoint.dispatch(new ServletHttpExchange(req, resp));
    }
}